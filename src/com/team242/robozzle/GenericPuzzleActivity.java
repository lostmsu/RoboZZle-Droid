/**
 * 
 */
package com.team242.robozzle;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.team242.robozzle.achievements.Achievement;
import com.team242.robozzle.model.Direction;
import com.team242.robozzle.model.ProgramState;
import com.team242.robozzle.model.Puzzle;
import com.team242.robozzle.model.Puzzle.Point;

import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.*;

//import com.j256.ormlite.android.apptools.OpenHelperManager.SqliteOpenHelperFactory;

/**
 * @author lost
 *
 */
public abstract class GenericPuzzleActivity extends AppCompatActivity {
//	static {
//		OpenHelperManager.setOpenHelperFactory(new SqliteOpenHelperFactory() {
//			public OrmLiteSqliteOpenHelper getHelper(Context context) {
//				return new RobozzleOffline(context);
//			}
//		});
//	}

	private RobozzleOffline databaseHelper = null;
	
	static Bitmap star;
	static Bitmap lowMem;
	protected static final Bitmap buffer = Bitmap.createBitmap(640, 480, Config.ARGB_8888);
	static final int KeepFree = 1359872;

	static Bitmap defaultBoard;
	static final HashMap<Integer, WeakReference<Bitmap>> boards = new HashMap<>();
	Paint paint = new Paint();
	protected SharedPreferences pref;

	public boolean loggedIn(){
		final String login = pref.getString(RoboZZleSettings.LOGIN, "");
		final String password = pref.getString(RoboZZleSettings.PASSWORD, "");

		return !login.equals("") && !password.equals("");
	}

	enum ThumbnailMemoryUsage{
		Normal,
		Limited,
		Disabled,
	}

	protected ThumbnailMemoryUsage thumbnailMemoryUsage = ThumbnailMemoryUsage.Normal;

	protected void openPuzzle(int puzzleID, java.lang.Class<?> activity) {
		Intent i = new Intent(this, activity);
		i.putExtra(Intents.PUZZLE_EXTRA, puzzleID);

		try{
			startActivityForResult(i, 42);
		}catch(Exception e){
			Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onDestroy(){
		if (this.databaseHelper != null){
			OpenHelperManager.releaseHelper();
			this.databaseHelper = null;
		}
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Context app = getApplicationContext();

		this.databaseHelper = OpenHelperManager.getHelper(this, RobozzleOffline.class);

		if (defaultBoard == null) {
			defaultBoard = BitmapFactory.decodeResource(app.getResources(), R.drawable.board);
		}
		
		if (star == null)
			star = BitmapFactory.decodeResource(app.getResources(), R.drawable.star);

		if (lowMem == null)
			lowMem = BitmapFactory.decodeResource(app.getResources(), R.drawable.lowmem);

		pref = getSharedPreferences(RoboZZleSettings.SHARED_PREFERENCES_NAME, 0);
	}

	public RobozzleOffline getHelper(){
		return this.databaseHelper;
	}
	
	static Bitmap getBoard(int cellSize){
		WeakReference<Bitmap> result = boards.get(cellSize);
		Bitmap bitmap;
		if (result != null){
			bitmap = result.get();
			if (bitmap != null) return bitmap;
			boards.remove(cellSize);
		}
		try {
			bitmap = Bitmap.createScaledBitmap(defaultBoard, 6 * cellSize, 4 * cellSize, true);
		}catch (OutOfMemoryError e){
			bitmap = lowMem;
		}
		boards.put(cellSize, new WeakReference<>(bitmap));
		return bitmap;
	}
	
	protected Bitmap drawThumbnail(Puzzle puzzle){
		if (puzzle == null) return null;
		switch (thumbnailMemoryUsage){
		case Disabled:
			return null;
		case Limited:
			Runtime runtime = Runtime.getRuntime();
			if (runtime.freeMemory() < KeepFree){
				System.gc();
				if (runtime.freeMemory() < KeepFree) return lowMem;
			}
			break;
		}

		if (puzzle.robotCol >= puzzle.getWidth() || puzzle.robotRow >= puzzle.getHeight())
			return null;
		
		// TODO: thumbnail memory optimization
		try{
			int bitmapSize = thumbnailMemoryUsage == ThumbnailMemoryUsage.Normal? 144: 96;

			final Bitmap result = Bitmap.createBitmap(bitmapSize, bitmapSize/16*12,
					thumbnailMemoryUsage == ThumbnailMemoryUsage.Normal
						? Config.ARGB_8888
						: Config.RGB_565);
			final Canvas canvas = new Canvas(result);
		
			drawBackground(canvas, bitmapSize/16, puzzle);
			drawStars(canvas, bitmapSize/16, puzzle.getStars(), true);
			drawRobot(canvas, bitmapSize/16, new Point(puzzle.robotCol, puzzle.robotRow), puzzle.robotDir);

			return result;
		}catch(OutOfMemoryError outOfMemoryError){
			return lowMem;
		}
	}

	protected Bitmap drawThumbnail(Puzzle puzzle, Bitmap result){
		if (result == lowMem) return drawThumbnail(puzzle);

		final Canvas canvas = new Canvas(result);

		int bitmapSize = thumbnailMemoryUsage == ThumbnailMemoryUsage.Normal? 144: 96;

		drawBackground(canvas, bitmapSize/16, puzzle);
		drawStars(canvas, bitmapSize/16, puzzle.getStars(), true);
		drawRobot(canvas, bitmapSize/16, new Point(puzzle.robotCol, puzzle.robotRow), puzzle.robotDir);

		return result;
	}

    public void openAchievements() {
        Intent i = new Intent(this, Achievements.class);

        try{
            startActivityForResult(i, 42);
        }catch(Exception e){
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

	public void checkAchievements(Achievement[] achievementsToCheck) {
		Puzzle[] allPuzzles = getAllPuzzles();
		List<Achievement> userWonAchievements = new ArrayList<>();
		List<Achievement> userLostAchievements = new LinkedList<>();

		for (Achievement achievement: achievementsToCheck) {
			boolean actualDone = achievement.isDone(allPuzzles);
			boolean storedDone = achievement.isStateSolved(pref);

			if (actualDone == storedDone) continue;

			if (actualDone){
				userWonAchievements.add(achievement);
			} else
				userLostAchievements.add(achievement);

			achievement.setState(pref, actualDone);
		}

//		// debug
//		userWonAchievements = Arrays.asList(Achievement.achievements).subList(0, Achievement.achievements.length/2);
//		userLostAchievements = Arrays.asList(Achievement.achievements).subList(Achievement.achievements.length/2, Achievement.achievements.length);
//		// --debug

		if (userWonAchievements.size() == 0 && userLostAchievements.size() == 0) {
			return; // no change in achievements
		}

		showAchievementsChangedDialog(userWonAchievements, userLostAchievements);
	}

	private void showAchievementsChangedDialog(List<Achievement> userWonAchievements, List<Achievement> userLostAchievements) {
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.achievement_dialog);
		dialog.setTitle(R.string.achievementsChangeTitle);

		showOrHideAchievementsList(userWonAchievements,
				(ListView) dialog.findViewById(R.id.achievementsWon),
				(TextView) dialog.findViewById(R.id.achievementWonTitle));

		showOrHideAchievementsList(userLostAchievements,
				(ListView) dialog.findViewById(R.id.achievementsLost),
				(TextView) dialog.findViewById(R.id.achievementLostTitle));

		View dialogButtonGoToAchievements = dialog.findViewById(R.id.openAchievements);
		dialogButtonGoToAchievements.setOnClickListener((new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				openAchievements();
				dialog.dismiss();
			}
		}));

		dialog.show();
	}

	private void showOrHideAchievementsList(List<Achievement> achievements, ListView listView, TextView textView) {
		if (achievements.size() > 0) {
			listView.setVisibility(View.VISIBLE);
			textView.setVisibility(View.VISIBLE);

			ListAdapter adapter = new AchievementAdapter(this,
					R.layout.achievement_changed_list_entry, achievements);
			listView.setAdapter(adapter);
		} else {
			listView.setVisibility(View.GONE);
			textView.setVisibility(View.GONE);
		}
	}

	public void checkAchievements() {
		checkAchievements(Achievements.achievements);
	}
	
	protected void drawRobot(Canvas canvas, int cellSize, Point pos, int direction){
		drawRobot(canvas, cellSize, pos.x, pos.y, direction);
	}
	
	protected void drawRobot(Canvas canvas, int cellSize, int x, int y, int direction){
		final Bitmap board = getBoard(cellSize);
		final Rect src = new Rect((1+direction) * cellSize, 0, (2 + direction) * cellSize, cellSize);
		final Rect dst = new Rect(x * cellSize, y * cellSize, (x + 1)*cellSize, (y + 1)*cellSize);
		canvas.drawBitmap(board, src, dst, paint);
	}

	protected void drawRobot(Canvas canvas, int cellSize, int x, int y, int direction, int frame, int frameCount){
		final Bitmap board = getBoard(cellSize);
		int dx = 0, dy = 0;
		int delta = frame * cellSize / frameCount;
		switch (direction){
			case Direction.DOWN:
				dy += delta;
				break;

			case Direction.TOP:
				dy -= delta;
				break;

			case Direction.LEFT:
				dx -= delta;
				break;

			default:
				dx += delta;
				break;
		}
		final Rect src = new Rect((1+direction) * cellSize, 0, (2 + direction) * cellSize, cellSize);
		final Rect dst = new Rect(x * cellSize + dx, y * cellSize + dy, dx + (x + 1)*cellSize, dy + (y + 1)*cellSize);
		canvas.drawBitmap(board, src, dst, paint);
	}
	
	protected void drawStars(Canvas canvas, int cellSize, Collection<Point> stars, boolean simplified){
		paintStars();
		if (simplified){
			
			for(Point star: stars){
				canvas.drawCircle(star.x * cellSize + cellSize / 2, star.y * cellSize + cellSize / 2, cellSize / 4, paint);
			}
		} else {
			Rect dest = new Rect();
			for(Point star: stars){
				dest.left = star.x * cellSize; dest.top = star.y * cellSize;
				dest.right = dest.left + cellSize; dest.bottom = dest.top + cellSize;
				canvas.drawBitmap(GenericPuzzleActivity.star, null, dest, paint);
			}
		}
	}

	void paintStars() {
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setColor(Color.YELLOW);
		paint.setStyle(Style.FILL);
	}
	
	protected void drawStar(Canvas canvas, int cellSize, int x, int y, boolean simplified){
		paintStars();
		if (simplified)
			canvas.drawCircle(x * cellSize + cellSize / 2, y * cellSize + cellSize / 2, cellSize / 4, paint);
		else{
			Rect dest = new Rect();
			dest.left = x * cellSize; dest.top = y * cellSize;
			dest.right = dest.left + cellSize; dest.bottom = dest.top + cellSize;
			canvas.drawBitmap(star, null, dest, paint);
		}
	}
	
	protected int pixels(int dip){
		final float scale = getBaseContext().getResources().getDisplayMetrics().density;
		return (int)(dip * scale);
	}
	
	protected void drawBackground(Canvas canvas, int cellSize, Puzzle puzzle){
		final Bitmap board = getBoard(cellSize);
		final Rect red = new Rect(0, cellSize, cellSize, cellSize * 2);
		final Rect green = new Rect(0, cellSize * 2, cellSize, cellSize * 3);
		final Rect blue = new Rect(0, cellSize * 3, cellSize, cellSize * 4);
		final Rect dest = new Rect();
		paint.setAntiAlias(false);
		paint.setDither(false);
		for(int y = puzzle.getHeight() - 1; y >= 0; y--){
			dest.top = y * cellSize;
			dest.bottom = (y + 1) * cellSize;
			String items = puzzle.getItems(y);
			String colors = puzzle.getColors(y);
			for(int x = items.length() - 1; x>=0; x--){
				dest.left = x * cellSize;
				dest.right = (x + 1) * cellSize;
				if (items.charAt(x) != '#'){
					Rect src = null;
					switch(colors.charAt(x)){
					case 'R':
						src = red;
						break;
					case 'G':
						src = green;
						break;
					case 'B':
						src = blue;
						break;
					}
					canvas.drawBitmap(board, src, dest, paint);
				} else {
					// TODO: do it by demand only
					canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null);
					canvas.clipRect(dest);
					canvas.drawColor(0, PorterDuff.Mode.CLEAR);
					canvas.restore();
				}
			}
		}
	}
	
	protected void drawCell(Canvas canvas, int cellSize, ProgramState state, Point point) {
		int x = point.x, y = point.y;
		drawBackground(canvas, cellSize, x, y, state.getColor(x, y));
		if (state.getStars().contains(point)) drawStar(canvas, cellSize, x, y, false);
		if (state.getPosition().equals(point)) drawRobot(canvas, cellSize, x, y, state.getDirection());
	}
	
	protected void drawBreakpoint(Canvas canvas, int cellSize, int x, int y){
		paintBreakpoints();
		
		canvas.drawRect(x * cellSize+2, y * cellSize+2, 
				x * cellSize + cellSize-2, y * cellSize + cellSize-2, paint);
	}
	
	protected void drawBreakpoints(Canvas canvas, int cellSize, Iterable<Point> breakpoints){
		paintBreakpoints();
		
		for(Point breakpoint: breakpoints)
			canvas.drawRect(breakpoint.x * cellSize+2, breakpoint.y * cellSize+2, 
					breakpoint.x * cellSize + cellSize-2, breakpoint.y * cellSize + cellSize-2, paint);
	}

	private void paintBreakpoints() {
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setColor(Color.WHITE);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(2);
	}
	
	
	
	protected void drawBackground(Canvas canvas, int cellSize, int x, int y, char color){
		final Bitmap board = getBoard(cellSize);
		final Rect dest = new Rect();
		final Rect src = new Rect(0, 0, cellSize, cellSize);
		
		dest.top = y * cellSize;
		dest.bottom = (y + 1) * cellSize;
		dest.left = x * cellSize;
		dest.right = (x + 1) * cellSize;
		
		paint.setAntiAlias(true);
		paint.setDither(true);
		
		switch (color) {
		case 'R':
			src.top = cellSize;
			src.bottom = cellSize * 2;
			break;
		case 'G':
			src.top = cellSize * 2;
			src.bottom = cellSize * 3;
			break;
		case 'B':
			src.top = cellSize * 3;
			src.bottom = cellSize * 4;
			break;

		default:
			canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null);
			canvas.clipRect(dest);
			canvas.drawARGB(0, 128, 128, 128);
			canvas.restore();
			return;
		}
		
		canvas.drawBitmap(board, src, dest, paint);
	}
	
	public Puzzle getPuzzle(int id){
		try{
			return getHelper().getPuzzlesDAO().queryForId(id);
		}catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public Puzzle[] getAllPuzzles(){
		try{
			List<Puzzle> puzzles = getHelper().getPuzzlesDAO().queryForAll();
			Puzzle[] puzzleArray = new Puzzle[puzzles.size()];
			puzzles.toArray(puzzleArray);
			return puzzleArray;
		}catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
