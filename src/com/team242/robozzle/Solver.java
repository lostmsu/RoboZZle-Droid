/**
 * 
 */
package com.team242.robozzle;

import RoboZZle.Telemetry.Actions.*;
import RoboZZle.Telemetry.SessionLog;
import RoboZZle.Telemetry.SessionLogWriter;
import RoboZZle.Telemetry.SolutionTelemetry;
import RoboZZle.Telemetry.TelemetryBag;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.*;
import androidx.appcompat.widget.Toolbar;
import android.widget.LinearLayout.LayoutParams;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.team242.robozzle.model.Program;
import com.team242.robozzle.model.ProgramState;
import com.team242.robozzle.model.ProgramState.GameEnd;
import com.team242.robozzle.model.ProgramState.IP;
import com.team242.robozzle.model.Puzzle;
import com.team242.robozzle.model.Puzzle.Point;
import com.team242.robozzle.telemetry.TelemetryClient;
import org.apache.commons.io.FileUtils;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @author lost
 *
 */
public class Solver extends GenericPuzzleActivity {
	static final String sessionLogExtension = ".session.log";
	public static final String STARTING_PROGRAM_FILE_NAME = "starting.code";
	public static final String TELEMETRY_VERSION_FILE_NAME = "version.txt";

	static final FilenameFilter DIRECTORIES_ONLY = new FilenameFilter() {
		@Override
		public boolean accept(File directory, String entryName) {
			return new File(directory, entryName).isDirectory();
		}
	};
	static final FilenameFilter SESSION_LOGS_ONLY = new FilenameFilter() {
		@Override
		public boolean accept(File directory, String fileName) {
			return fileName.endsWith(sessionLogExtension);
		}
	};

	SessionLog sessionLog;
	SessionLogWriter sessionLogWriter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		try{
			setContentView(R.layout.solver);
		}catch (Exception e) {
			assert false;
		}

		pref = getSharedPreferences(RoboZZleSettings.SHARED_PREFERENCES_NAME, 0);
		
		colors[0] = '_';
		actions[0] = actions[12] = '\0';
		actions[1] = 'F';
		actions[2] = 'L';
		actions[3] = 'R';
		actions[4] = '1';

		stackText = (TextView)findViewById(R.id.stackText);

		puzzleView = (WidthImageView)findViewById(R.id.puzzleView);
		puzzleView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (v.getVisibility() != View.VISIBLE) return true;
				
				if (isRunning()){
					switch(event.getAction()){
					case MotionEvent.ACTION_DOWN:
						speedup = true;
						frames = 1;
						return true;
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_UP:
						speedup = false;
						frames = 1;
						return true;
					}
				} else {
					switch (event.getAction()){
					case MotionEvent.ACTION_DOWN:
						int w = v.getWidth(), h = v.getHeight();
						int x = (int)event.getX() * Puzzle.WIDTH / w, y = (int)event.getY() * Puzzle.HEIGHT / h;
						if (puzzle.isBlack(x, y)) return false;
						toggleBreakpoint(x, y);
						break;
					}
					
				}
				
				return false;
			}
		});

		canvas = new Canvas(buffer);
		
		programInput = (LinearLayout)findViewById(R.id.commands);
		
		createControlsButtons();
		initHelp();
		
		initCommandDialog();

		if (this.telemetryNeedsUpgrade()){
			this.deleteAllTelemetry();
			this.addTelemetryVersion();
		}

		tools = (ViewFlipper)findViewById(R.id.solverTools);
		initWinTools();

		onNewIntent(getIntent());
	}

	View winDone, winEdit;
	View winToolsSocial;
	boolean difficultyTouched;

	private void initWinTools() {
		winDone = findViewById(R.id.winDone);
		winDone.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (loggedIn() && winToolsSocial.getVisibility() == View.VISIBLE){
					submitVote();
					if (isRobAiTelemetryEnabled())
						submitRobAiTelemetry();
				}
				finish();
			}
		});

		winEdit = findViewById(R.id.winEdit);
		winEdit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				tools.showPrevious();
			}
		});

		winToolsSocial = findViewById(R.id.winToolsSocial);

		iLike = (CheckBox)findViewById(R.id.iLike);
		difficulty = (SeekBar)findViewById(R.id.difficulty);

		difficulty.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				difficultyTouched = true;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});
	}

	CheckBox iLike;
	SeekBar difficulty;

	private void submitVote() {
		final String login = pref.getString(RoboZZleSettings.LOGIN, "");
		final String password = pref.getString(RoboZZleSettings.PASSWORD, "");

		if (login.equals("") || password.equals("")){
			Toast.makeText(this, R.string.provideLogin, Toast.LENGTH_LONG);
			return;
		}

		final int like = iLike.isChecked()? 1: 0;
		final int diff = Math.max(1,(difficulty.getProgress() + 19) / 20);
		puzzle.setUserDifficulty(diff);
		puzzle.setUserLike(like);

		try {
			getHelper().getPuzzlesDAO().update(puzzle);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		AsyncTask<Void, Void, Void> submitter = new AsyncTask<Void, Void, Void>() {
			int message = 0;

			@Override
			protected void onPostExecute(Void result) {
				if (isCancelled() || message == 0) return;

				Toast.makeText(Solver.this, message, Toast.LENGTH_LONG).show();
			}

			@Override
			protected Void doInBackground(Void... params) {
				RobozzleWebClient webClient = new RobozzleWebClient();
				try{
					webClient.SubmitLevelVote(puzzle.id, login, password, like, diff);
				}catch (NoSuchAlgorithmException e) {
					// TODO: report exception
					message = R.string.loginHashNotSupported;
				} catch (IOException e) {
					message = R.string.robozzleComIOError;
				} catch (XmlPullParserException e) {
					message = R.string.loginCantParseServerResponse;
				}

				return null;
			}
		};

		submitter.execute();
	}

	private boolean isRunning(){
		return pauseButton.getVisibility() == View.VISIBLE;
	}
	
	private boolean toggleBreakpoint(int x, int y){
		Point point = new Point(x, y);
		boolean existed = breakpoints.contains(point);
		
		if (existed) breakpoints.remove(point);
		else breakpoints.add(point);
		
		drawCell(canvas, 40, state, point);
		
		if (!existed) {
			drawBreakpoint(canvas, 40, x, y);
		}
		
		flush();
		
		return existed;
	}
	
	private void createControlsButtons() {
		controls = findViewById(R.id.controls);
		editControls = findViewById(R.id.editControls);
		speedButtonContainer = findViewById(R.id.speedButtonContainer);
		playButton = (ImageButton)findViewById(R.id.playButton);
		playButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.getVisibility() != View.VISIBLE) return;
				
				run();
				sessionLogWriter.LogPlayStart(state.getTotalSteps());
			}
		});
		pauseButton = (ImageButton)findViewById(R.id.pauseButton);
		pauseButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (v.getVisibility() != View.VISIBLE) return;
				
				pause();
				sessionLogWriter.LogPlayEnd(state.getTotalSteps());
			}
		});
		stopButton = (ImageButton)findViewById(R.id.stopButton);
		stopButton.setEnabled(false);
		stopButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (v.getVisibility() != View.VISIBLE) return;

				stop();
			}
		});
		
		stepButton = (ImageButton)findViewById(R.id.stepButton);
		stepButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (v.getVisibility() != View.VISIBLE) return;

				if (isRunning())
					sessionLogWriter.LogPlayEnd(state.getTotalSteps());

				stopButton.setEnabled(true);
				stopButton.setBackgroundResource(R.drawable.ic_media_stop);
				sessionLogWriter.Log(StepCommand.getInstance());
				step();
			}
		});
		
		undoButton = (ImageButton)findViewById(R.id.undoButton);
		undoButton.setEnabled(false);
		undoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (v.getVisibility() != View.VISIBLE) return;

				sessionLogWriter.Log(UndoCommand.getInstance());
				undo();
			}
		});
		redoButton = (ImageButton)findViewById(R.id.redoButton);
		redoButton.setEnabled(false);
		redoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (v.getVisibility() != View.VISIBLE) return;

				sessionLogWriter.Log(RedoCommand.getInstance());
				redo();
			}
		});

		speedSlider = (SeekBar) findViewById(R.id.speed);
		speedSlider.setProgress(pref.getInt(RoboZZleSettings.SPEED, 50));
		setSpeed(speedSlider.getProgress());
		speedSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				setSpeed(i);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) { }
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				speedSlider.setVisibility(View.GONE);
				controls.setVisibility(View.VISIBLE);
			}
		});

		speedButton = (ImageButton)findViewById(R.id.speedButton);
		speedButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				speedSlider.setVisibility(View.VISIBLE);
				controls.setVisibility(View.INVISIBLE);
			}
		});
	}

	SharedPreferences pref;
	
	Dialog helpDialog;
	void initHelp()
	{
		helpDialog = new Dialog(this);
		helpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		helpDialog.setContentView(R.layout.help);
		
		boolean isFirstTime = pref.getBoolean(RoboZZleSettings.FIRST_TIME_SOLVER, true);
		if (isFirstTime) helpDialog.show();
		Editor edit = pref.edit();
		edit.putBoolean(RoboZZleSettings.FIRST_TIME_SOLVER, false);
		edit.commit();
	}
	
	ViewFlipper tools;
	
	enum MoveDirection
	{
		Backward,
		Forward
	}
	
	int currentHistoryItem = -1;
	
	void initCommandDialog() {
		commandDialog = new Dialog(this);
		commandDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		commandDialog.setContentView(R.layout.command_dialog);
		// commandDialog.setTitle(R.string.commandDialog);
		
		commandsLeft = (ImageButton)commandDialog.findViewById(R.id.commandsLeft);
		commandsRight = (ImageButton)commandDialog.findViewById(R.id.commandsRight);
		
		OnClickListener moveListener = new OnClickListener() {
			public void onClick(View v) {
				if (v.getVisibility() != View.VISIBLE) return;
				
				commandDialog.dismiss();
				moveCommands(v == commandsLeft? MoveDirection.Backward: MoveDirection.Forward);
			}
		};
		commandsRight.setOnClickListener(moveListener);
		commandsLeft.setOnClickListener(moveListener);
		
		commandSelector = (ImageView)commandDialog.findViewById(R.id.commandSelector);
		commandDialog.setOnDismissListener(new OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				commandCells[cfunc][ccmd].setBackgroundColor(Color.TRANSPARENT);
			}
		});
		commandSelector.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (v.getVisibility() != View.VISIBLE) return true;
				
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					float cellWidth = commandSelector.getWidth() * 1.0f / cw;
					float cellHeight = commandSelector.getHeight() * 1.0f / ch;
					int x = (int)(event.getX()/cellWidth);
					int y = (int)(event.getY()/cellHeight);
					try{
						Bitmap bmp = Bitmap.createBitmap(currentCommands, x * cellSize, y * cellSize, cellSize, cellSize, null, true);
						commandImages[cfunc][ccmd].setImageBitmap(bmp);
						commandImages[cfunc][ccmd].setVisibility(View.VISIBLE);
						commandDialog.dismiss();
						String oldCommand = program.actions[cfunc][ccmd] + "" + program.colors[cfunc][ccmd];

						program.actions[cfunc][ccmd] = actions[x];
						program.colors[cfunc][ccmd] = colors[y];

						String newCommand = colors[y] + "" + actions[x];

						EditCommand editCommand = new EditCommand();
						editCommand.setFunction(cfunc);
						editCommand.setCommandOffset(ccmd);
						editCommand.setOldCommand(oldCommand);
						editCommand.setNewCommand(newCommand);
						sessionLogWriter.Log(editCommand);
					}catch(IllegalArgumentException illegalArg){
						IllegalArgumentException detailed = new IllegalArgumentException(
								"x = " + x + "; y = " + y + "; e.x = " + event.getX() + "; e.y = " + event.getY() +
								"\ns.width = " + currentCommands.getWidth() + "; s.height = " + currentCommands.getHeight()+
								"\ncommandSelector.width = " + commandSelector.getWidth() + "; cw = " + cw + "; ch = " + ch +
								"\ny * cellSize + cellSize must be <= s.height");
						// TODO: report exception
					}
					try {
						programChanged();
					} catch (SQLException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
					return true;

				default:
					return false;
				}
			}
		});
	}
	
	void moveCommands(MoveDirection moveDirection) {
		int hole = findHole(moveDirection);
		
		if (hole < 0){
			Exception error = new IllegalStateException("Commands can't be moved here!\n"+
					"Program: " + program.getProgram() + "\n" +
					"Func: " + cfunc + " Command: " + ccmd + " Direction: " + moveDirection);
			// TODO: report exception
			return;
		}
		
		if (hole < ccmd)
			for(int i = hole; i < ccmd; i++){
				program.actions[cfunc][i] = program.actions[cfunc][i + 1];
				program.colors[cfunc][i] = program.colors[cfunc][i + 1];
			}
		else for (int i = hole; i > ccmd; i--){
			program.actions[cfunc][i] = program.actions[cfunc][i - 1];
			program.colors[cfunc][i] = program.colors[cfunc][i - 1];
		}
		
		program.actions[cfunc][ccmd] = '\0';
		program.colors[cfunc][ccmd] = '\0';
		
		onProgramChanged();
	}

	private void onProgramChanged() {
		try {
			programChanged();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		initProgram();
	}

	private int findHole(MoveDirection moveDirection) {
		if (program.actions[cfunc][ccmd] == '\0') return -1;
		switch (moveDirection) {
		case Backward:
			for(int i = ccmd - 1; i >= 0; i--)
				if (program.actions[cfunc][i] == '\0'){
					return i;
				}
			break;

		default:
			for(int i = ccmd + 1; i < program.getFunctionLength(cfunc); i++)
				if (program.actions[cfunc][i] == '\0'){
					return i;
				}
			break;
		}
		return -1;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		if (intent.hasExtra(Intents.PUZZLE_EXTRA))
			loadPuzzle(intent.getIntExtra(Intents.PUZZLE_EXTRA, 0));
	}
	
	AsyncTask<Void, Void, Void> stepper;
	boolean speedup = false;
	int getStepTime(){
		return speedup ? InfiniteSpeedStepTime : NormalSpeedStepTime;
	}

	void createStepper() {
		stepper = new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPostExecute(Void result) {
				if (pauseButton.getVisibility() != View.VISIBLE) {
					if (!stopButton.isEnabled()){
						if (!state.isFunctionEnded()) {
							IP cip = state.getIP();
							commandCells[cip.function][cip.command].setBackgroundColor(Color.TRANSPARENT);
						}
						playButton.setVisibility(View.VISIBLE);
					}
					return;
				}
				step();
			}
			
			@Override
			protected Void doInBackground(Void... params) {
				int sleptFor = 0;
				while (sleptFor < getStepTime())
					try {
						int sleep = Math.max(Math.min(getStepTime() - sleptFor, 10), 1);
						Thread.sleep(sleep);
						sleptFor += sleep;
					} catch (InterruptedException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				
				return null;
			}
		};
	}
	
	LinearLayout programInput;
	ImageView commandSelector;
	WidthImageView puzzleView;
	Puzzle puzzle;
	Program program;
	ProgramState state;
	HashSet<Point> breakpoints = new HashSet<>();

	Canvas canvas;
	Dialog commandDialog;
	ImageButton playButton, pauseButton, stopButton, stepButton, commandsLeft, commandsRight,
		undoButton, redoButton, speedButton;
	TextView stackText;
	View controls;
	View editControls, speedButtonContainer;
	SeekBar speedSlider;

	static Bitmap commands;
	static int cellSize = 1;
	private Bitmap getCommandsBitmap(){
		if (commands == null){
			Context app = getApplicationContext();

			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			commands = BitmapFactory.decodeResource(app.getResources(), R.drawable.commands);
			
			while (cellSize * 13 < 390 * metrics.densityDpi / 160) cellSize++;
			commands = Bitmap.createScaledBitmap(commands, cellSize * 13, cellSize * 4, false);
		}
		return commands;
	}
	Bitmap currentCommands;

	private void loadPuzzle(int puzzleID) {
		try{
			try {
				puzzle = getHelper().getPuzzlesDAO().queryForId(puzzleID);
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

			Toolbar toolbar = (Toolbar)findViewById(R.id.solverToolbar);
			toolbar.setTitle(puzzle.title);
			this.setSupportActionBar(toolbar);

			program = puzzle.getCurrentProgram();
			state = new ProgramState(puzzle);
			state.setCallListener(new ProgramState.CallListener() {
				@Override
				public void onFunctionEnter(int func) {
					String callEntry = "F" + (func + 1) + "\n";
					if (stackText.length() == 2)
						stackText.setText(callEntry);
					else
						stackText.append(callEntry);
				}

				@Override
				public void onFunctionExit() {
					String currentStack = stackText.getText().toString();
					if (currentStack.length() == 0)
						return;
					int prev = currentStack.lastIndexOf("\n", currentStack.length() - 2);
					if (prev < 0) {
						stackText.setVisibility(View.INVISIBLE);
						stackText.setText("__");
					} else
						stackText.setText(currentStack.substring(0, prev + 1));
				}
			});
	
			difficulty.setProgress(puzzle.getUserDifficulty() == 0
					? puzzle.difficulty
					: puzzle.getUserDifficulty() * 20);
			iLike.setChecked(puzzle.getUserLike() > 0);
			
			initCommands();
			initProgram();
			initHistory();
			
			try{
				// drawBackground();
				drawPuzzle();
				
				flush();
			}catch(Exception e)
			{
				Toast.makeText(Solver.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			
			if (puzzleID < 0) helpDialog.show();
			
			if (load != null) load.setEnabled(puzzle.solution != null);
		} catch (Exception e){
			Toast.makeText(Solver.this, R.string.puzzleLoadFail, Toast.LENGTH_LONG).show();
			Exception puzzleError = new PuzzleException(puzzleID, e);
			// TODO: report exception
			finish();
		}
	}

	private void initHistory() {
		currentHistoryItem = -1;
		
		Dao<EditHistoryItem, Integer> historyDAO = getHelper().getHistoryDAO();
		QueryBuilder<EditHistoryItem, Integer> query = historyDAO.queryBuilder();
		try{
			query.where().eq("puzzle_id", puzzle.id);
			query.orderBy("id", false);
			query.limit(2L);
			
			List<EditHistoryItem> latest = historyDAO.query(query.prepare());
			redoButton.setEnabled(false);
			redoButton.setBackgroundResource(android.R.drawable.ic_menu_revert);
			undoButton.setEnabled(false);
			undoButton.setBackgroundResource(android.R.drawable.ic_menu_revert);
			if (latest.size() == 0){
				createHistoryItemOfCurrentProgram(historyDAO);
				return;
			}
			if (latest.get(0).program.equals(program.getProgram())){
				currentHistoryItem = latest.get(0).id;
				if (latest.size() > 1){
					undoButton.setEnabled(true);
					undoButton.setBackgroundResource(android.R.drawable.ic_menu_revert);
				} else{
					undoButton.setEnabled(false);
					undoButton.setBackgroundResource(android.R.drawable.ic_menu_revert);
				}
			} else{
				undoButton.setEnabled(true);
				undoButton.setBackgroundResource(android.R.drawable.ic_menu_revert);
				createHistoryItemOfCurrentProgram(historyDAO);
			}
		}catch(SQLException e) {
			Toast.makeText(this, R.string.historyLoadFail, Toast.LENGTH_LONG).show();
		}
	}

	private void createHistoryItemOfCurrentProgram(
			Dao<EditHistoryItem, Integer> historyDAO) throws SQLException {
		EditHistoryItem current = new EditHistoryItem();
		current.program = program.getProgram();
		current.puzzle = puzzle;

		historyDAO.create(current);
		currentHistoryItem = current.id;
	}

	@Override
	public void onBackPressed() {
		if (speedSlider.getVisibility() == View.VISIBLE) {
			speedSlider.setVisibility(View.GONE);
			controls.setVisibility(View.VISIBLE);
			return;
		}

		Dao<EditHistoryItem, Integer> historyDAO = getHelper().getHistoryDAO();
		QueryBuilder<EditHistoryItem, Integer> query = historyDAO.queryBuilder();
		try {
			query.where().eq("puzzle_id", puzzle.id);
			query.orderBy("id", false);
			query.limit(2L);

			List<EditHistoryItem> latest = historyDAO.query(query.prepare());
			if (latest.size() <= 1 && program.isEmpty()) {
				puzzle.imScared();
				Dao<Puzzle, Integer> puzzleDao = getHelper().getPuzzlesDAO();
				puzzleDao.update(puzzle);

				Intent i = new Intent();
				i.putExtra(Intents.PUZZLE_SCARY, puzzle.id);
				setResult(RESULT_OK, i);
			}
		} catch (SQLException e) {
			Toast.makeText(this, R.string.historyLoadFail, Toast.LENGTH_LONG).show();
		}

		super.onBackPressed();
	}

	void drawPuzzle() {
		drawBackground(canvas, 40, puzzle);
		drawStars(canvas, 40, puzzle.getStars(), false);
		drawRobot(canvas, 40, new Point(puzzle.robotCol, puzzle.robotRow), puzzle.robotDir);
		drawBreakpoints(canvas, 40, breakpoints);
	}

	int prevX, prevY, prevDirection;
	char prevColor;
	int prevFunction, prevInstruction;
	int frame = 1, frames = 1;
	GameEnd finish = GameEnd.No;

	private void storePrevious(){
		prevX = state.getX(); prevY = state.getY(); prevDirection = state.getDirection();
		prevColor = state.getColor(prevX, prevY);
		frame = 1;
		IP prevIP = state.getIP();
		prevFunction = prevIP.function;
		prevInstruction = prevIP.command;
		commandCells[prevFunction][prevInstruction].isClickable();
	}

	boolean activeActivity = true;

	@Override
	protected void onDestroy(){
		super.onDestroy();
		activeActivity = false;
	}

	@Override
	protected void onResume(){
		super.onResume();
		activeActivity = true;
		this.startTelemetrySession();
	}

	@Override
	protected void onPause(){
		this.stopTelemetrySession();
		super.onPause();
	}

	private void startTelemetrySession(){
		this.sessionLog = new SessionLog();
		this.sessionLog.setPuzzleID(this.puzzle.id);
		this.sessionLogWriter = new SessionLogWriter(this.sessionLog);
		Log.d("ROB AI", "new session");

		String directory = this.ensureTelemetryDirectory();
		File startingProgramFile = new File(directory + STARTING_PROGRAM_FILE_NAME);
		if (!startingProgramFile.exists()){
			writeAllText(startingProgramFile.getPath(), this.program.getProgram());
		}
	}

	private void stopTelemetrySession(){
		if (this.puzzle == null){
			Log.w("ROB AI", "no puzzle at the end of session");
			return;
		}

		this.saveTelemetrySession();
		Log.d("ROB AI", "end of session");
	}

	private String ensureTelemetryDirectory(){
		String directory = this.getApplicationContext().getFilesDir() + "/Telemetry/Puzzles/" + this.puzzle.id + "/";
		File file = new File(directory);
		file.mkdirs();
		return directory;
	}

	private void writeAllText(String filePath, String text){
		try {
			OutputStream out = new FileOutputStream(filePath, /*append:*/ false);
			out.write(text.getBytes("UTF8"));
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveTelemetrySession() {
		String log = new TelemetryClient().serialize(this.sessionLog);
		String directory = this.ensureTelemetryDirectory();
		String filePath = directory + "" + Long.toHexString(this.sessionLog.getStartTime().getTime()) + sessionLogExtension;
		this.writeAllText(filePath, log);
	}

	private void completeSolutionTelemetry(){
		final String sessionsDirectoryPath = this.getApplicationContext().getFilesDir() + "/Telemetry/Puzzles/" + this.puzzle.id + "/";
		File sessionsDirectory = new File(sessionsDirectoryPath);
		File[] sessionFiles = sessionsDirectory.listFiles(SESSION_LOGS_ONLY);
		Arrays.sort(sessionFiles);

		String solutionStart = sessionFiles[0].getName().replace(sessionLogExtension, "");
		String solutionDirectoryPath = this.getApplicationContext().getFilesDir()
				+ "/Telemetry/Solutions/" + this.puzzle.id + "/" + solutionStart + "/";
		File solutionDirectory = new File(solutionDirectoryPath);
		solutionDirectory.mkdirs();

		for(File session : sessionFiles){
			session.renameTo(new File(solutionDirectoryPath, session.getName()));
		}

		File startingProgramFile = new File(sessionsDirectoryPath + STARTING_PROGRAM_FILE_NAME);
		startingProgramFile.renameTo(new File(solutionDirectoryPath + STARTING_PROGRAM_FILE_NAME));
	}

	TelemetryBag readAllSolutionTelemetry(){
		TelemetryBag result = new TelemetryBag();
		File solutionPuzzlesDirectory = new File(this.getApplicationContext().getFilesDir() + "/Telemetry/Solutions/");
		File[] solutionPuzzles = solutionPuzzlesDirectory.listFiles(DIRECTORIES_ONLY);
		for(File solvedPuzzleDirectory: solutionPuzzles) {
			int puzzleId = Integer.parseInt(solvedPuzzleDirectory.getName());
			File[] solutions = solvedPuzzleDirectory.listFiles(DIRECTORIES_ONLY);
			for(File solutionDirectory: solutions){
				SolutionTelemetry solution = new SolutionTelemetry();
				solution.setSource(TelemetryClient.telemetrySource);
				solution.setPuzzleID(puzzleId);
				File startingProgramFile = new File(solutionDirectory, STARTING_PROGRAM_FILE_NAME);
				try {
					solution.setStartingProgram(FileUtils.readFileToString(startingProgramFile, "UTF8"));
					File[] sessionFiles = solutionDirectory.listFiles(SESSION_LOGS_ONLY);
					Arrays.sort(sessionFiles);
					for (File sessionFile : sessionFiles) {
						String serializedSession = FileUtils.readFileToString(sessionFile, "UTF8");
						SessionLog session = TelemetryClient.deserialize(serializedSession);
						solution.getSessions().add(session);
					}
					result.getSolutions().add(solution);
				}catch (IOException e){
					Log.e("ROB AI", "Can't read a solution", e);
				}
			}
		}
		return result;
	}

	void deleteAllTelemetry(){
		File telemetryDirectory = new File(this.getApplicationContext().getFilesDir() + "/Telemetry/");
		try {
			FileUtils.deleteDirectory(telemetryDirectory);
		}catch (IOException e){
			Log.e("ROB AI", "Failed to delete out of date telemetry", e);
			return;
		}

		Log.i("ROB AI", "All telemetry deleted");
	}

	void addTelemetryVersion(){
		File telemetryDirectory = new File(this.getApplicationContext().getFilesDir() + "/Telemetry/");
		telemetryDirectory.mkdirs();
		File telemetryVersionFile = new File(telemetryDirectory, TELEMETRY_VERSION_FILE_NAME);
		try {
			if (!telemetryVersionFile.exists())
				FileUtils.writeStringToFile(telemetryVersionFile, TelemetryClient.telemetrySource.getVersion(), "UTF8");
		}catch (IOException e){
			Log.e("ROB AI", "Can't update telemetry version", e);
			return;
		}

		Log.i("ROB AI", "Set telemetry version to " + TelemetryClient.telemetrySource.getVersion());
	}

	boolean telemetryNeedsUpgrade(){
		File telemetryDirectory = new File(this.getApplicationContext().getFilesDir() + "/Telemetry/");

		File telemetryVersionFile = new File(telemetryDirectory, TELEMETRY_VERSION_FILE_NAME);
		if (!telemetryVersionFile.exists()){
			return true;
		} else {
			try{
				String versionString = FileUtils.readFileToString(telemetryVersionFile, "UTF8");
				return !TelemetryClient.telemetrySource.getVersion().equals(versionString.trim());
			} catch (IOException e){
				Log.e("ROB AI", "Failed to read telemetry version", e);
				return true;
			}
		}
	}

	void submitRobAiTelemetry(){
		// just a safeguard
		if (!loggedIn())
			return;

		File telemetryDirectory = new File(this.getApplicationContext().getFilesDir() + "/Telemetry/");

		final File solutionsDirectory =  new File(telemetryDirectory, "Solutions/");
		final String login = pref.getString(RoboZZleSettings.LOGIN, "");
		final String password = pref.getString(RoboZZleSettings.PASSWORD, "");
		AsyncTask<Void, Void, Integer> submitter = new AsyncTask<Void, Void, Integer>() {
			@Override
			protected void onPostExecute(Integer message){
				if (message == 0)
					return;

				Toast.makeText(Solver.this, message, Toast.LENGTH_LONG).show();
			}

			@Override
			protected Integer doInBackground(Void... voids) {
				final TelemetryBag telemetry = readAllSolutionTelemetry();
				int message = 0;

				TelemetryClient client = new TelemetryClient();
				try {
					if (telemetry.getSolutions() != null && !telemetry.getSolutions().isEmpty()) {
						client.submit(telemetry, login, password);
						Log.i("ROB AI", "submitted some telemetry");
					}else {
						Log.i("ROB AI", "no telemetry to submit");
					}
				} catch (IOException e){
					message = R.string.telemetrySubmitIoError;
					Log.w("ROB AI", "Network error submitting telemetry", e);
				} catch (RuntimeException e){
					message = R.string.telemetrySubmitUnknownError;
					Log.e("ROB AI", "Runtime exception submitting telemetry", e);
				}

				try {
					FileUtils.deleteDirectory(solutionsDirectory);
				}catch (IOException e){
					Log.w("ROB AI", "Failed to delete submitted solutions", e);
				}
				return message;
			}
		};
		submitter.execute();
	}
	
	private void step(){
		if (!activeActivity) return;

		stackText.setVisibility(View.VISIBLE);
		editControls.setVisibility(View.GONE);
		speedButtonContainer.setVisibility(View.VISIBLE);

		if (frame < frames){
			drawBackground(canvas, 40, prevX, prevY, prevColor);
			if(breakpoints.contains(state.getPosition())) drawBreakpoint(canvas, 40, prevX, prevY);
			if (state.getX() != prevX || state.getY() != prevY){
				drawBackground(canvas, 40, state.getX(), state.getY(), state.getColor(state.getX(), state.getY()));
				if(breakpoints.contains(state.getPosition())) drawBreakpoint(canvas, 40, state.getX(), state.getY());
				drawRobot(canvas, 40, prevX, prevY, state.getDirection(), frame, frames);
			} else{
				drawRobot(canvas, 40, prevX, prevY, prevDirection);
			}
			
			flush();

			frame++;
			createStepper();
			stepper.execute();
			return;
		}

		int cx = prevX, cy = prevY;
		drawBackground(canvas, 40, cx, cy, state.getColor(cx, cy));
		if(breakpoints.contains(new Point(cx, cy)))
			drawBreakpoint(canvas, 40, cx, cy);
		IP cip = new IP(prevFunction, prevInstruction);

		try{
			commandCells[cip.function][cip.command].setBackgroundColor(Color.TRANSPARENT);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException(
					"Invalid IP " + cip.function + "," + cip.command + " on\n" +
					"Puzzle: " + puzzle.title + "; Program: " + program.getProgram() + "\n" +
					"Robot position: " + cx + "," + cy + "; direction: " + state.getDirection());
		}
		
		int x = state.getX(), y = state.getY(), dir = state.getDirection();
		
		Point pos = new Point(x, y);

		if (Puzzle.valid(x, y)){
			drawBackground(canvas, 40, x, y, state.getColor(x, y));
			drawRobot(canvas, 40, x, y, dir);
			if (breakpoints.contains(pos))
				drawBreakpoint(canvas, 40, x, y);
		}
		
		if (finish == GameEnd.No) {
			cip = state.getIP();
			commandCells[cip.function][cip.command].setBackgroundColor(Color.WHITE);
			if (breakpoints.contains(pos) && isRunning()) pause();
			storePrevious();
			finish = state.step();
			createStepper();
			stepper.execute();
		} else {
			if (state.isWin()){
				playButton.setVisibility(View.VISIBLE);
				win();
			} else {
				int messageID;
				switch(finish){
				case Fall:
					messageID = R.string.lostFall;
					break;
				case ProgramEnded:
					messageID = R.string.lostProgramEnded;
					break;
				case MaxStepsReached:
					messageID = R.string.lostOutOfSteps;
					break;
				default:
					messageID = R.string.lostStackOverflow;
					break;
				}
				Toast.makeText(Solver.this, messageID, Toast.LENGTH_LONG).show();
				playButton.setVisibility(View.VISIBLE);
			}

			stop();
			return;
		}
		
		flush();
	}

	private void win() {
		IP cip;
		cip = state.getIP();
		commandCells[cip.function][cip.command].setBackgroundColor(Color.WHITE);
		
		puzzle.setSolution(program.getProgram());
		try {
			getHelper().getPuzzlesDAO().update(puzzle);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		Intent i = new Intent();
		i.putExtra(Intents.PUZZLE_SOLVED, puzzle.id);
		setResult(RESULT_OK, i);

		winToolsSocial.setVisibility(loggedIn()? View.VISIBLE: View.GONE);
		difficultyTouched = false;

		tools.showNext();

		this.saveTelemetrySession();
		this.completeSolutionTelemetry();
	}

	boolean isRobAiTelemetryEnabled(){
		return pref.getBoolean(RoboZZleSettings.ROB_AI_TELEMETRY_ENABLED, RoboZZleSettings.ROB_AI_TELEMETRY_DEFAULT);
	}

	private void flush() {
		puzzleView.setDrawable(new BitmapDrawable(buffer));
	}

	static final int COMMAND_SIZE = 32;
	static final int IMAGE_SIZE = 30;
	static final int TEXT_SIZE = 30;
	
	private void initProgram() {
		programInput.removeAllViews();
		
		commandImages = new ImageView[puzzle.getFunctionCount()][];
		commandCells = new RelativeLayout[puzzle.getFunctionCount()][];
		int currOrient = getResources().getConfiguration().orientation;
		for(int i = 1; i <= puzzle.getFunctionCount(); i++){
			final int len = puzzle.getFunctionLength(i - 1);
			commandImages[i - 1] = new ImageView[len];
			commandCells[i - 1] = new RelativeLayout[len];
			TextView fname = new TextView(this);
			fname.setText("F" + i);
			fname.setVisibility(len > 0? View.VISIBLE: View.GONE);
			programInput.addView(fname, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			
			LinearLayout fline = null;
			for(int cmd = 0; cmd < len; cmd++){
				if (cmd % 5 == 0) {
					if (currOrient == Configuration.ORIENTATION_LANDSCAPE || cmd == 0){
						fline = new LinearLayout(this);
						fline.setOrientation(LinearLayout.HORIZONTAL);
						programInput.addView(fline, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					}
				}
				
				RelativeLayout bbox = new RelativeLayout(this);
				bbox.setGravity(Gravity.CENTER);
				if (fline != null) {
					fline.addView(bbox, pixels(COMMAND_SIZE), pixels(COMMAND_SIZE));
				}
				commandCells[i-1][cmd] = bbox;
				
				TextView cname = new TextView(this);
				cname.setText("" + (cmd + 1));
				cname.setGravity(Gravity.CENTER);
				cname.setBackgroundColor(0xFFA0A0A0);
				cname.setTextColor(0xFF000000);
				bbox.addView(cname, pixels(TEXT_SIZE), pixels(TEXT_SIZE));
				
				ImageView cimg = new ImageView(this);
				if (program.actions[i-1][cmd] != '\0'){
					if (program.actions[i-1][cmd] == '0')
						program.actions[i-1][cmd] = '1';
					int command = getCommandIndex(program.actions[i-1][cmd]);
					int color = getColorIndex(program.colors[i-1][cmd]);
					Bitmap bmp = Bitmap.createBitmap(currentCommands, command * cellSize, color * cellSize, cellSize, cellSize, null, true);
					cimg.setImageBitmap(bmp);
					cimg.setVisibility(View.VISIBLE);
				} else
					cimg.setVisibility(View.GONE);
				commandImages[i - 1][cmd] = cimg;
				bbox.addView(cimg, pixels(IMAGE_SIZE), pixels(IMAGE_SIZE));
				
				final int ffnc = i - 1;
				final int fcmd = cmd;
				
				OnClickListener clickHandler = new OnClickListener() {
					public void onClick(View v) {
						if (v.getVisibility() != View.VISIBLE) return;
						
						if (checkRunning()) return;
						
						cfunc = ffnc;
						ccmd = fcmd;
						commandCells[cfunc][ccmd].setBackgroundColor(Color.WHITE);
						commandsLeft.setVisibility(findHole(MoveDirection.Backward) < 0
								? View.GONE: View.VISIBLE);
						commandsRight.setVisibility(findHole(MoveDirection.Forward) < 0
								? View.GONE: View.VISIBLE);
						commandDialog.show();
					}
				}; 
				cname.setOnClickListener(clickHandler);
				cimg.setOnClickListener(clickHandler);
			}
		}
	}
	
	int cfunc;
	int ccmd;
	ImageView[][] commandImages;
	RelativeLayout[][] commandCells;

	private void initCommands() {
		//commandDialog.setTitle(getString(R.string.commandDialog) + " " + Integer.toHexString(puzzle.allowedCommands));
		
		drawCommands();
		
		commandSelector.setImageBitmap(currentCommands);
		
//		TextView v = (TextView)findViewById(R.id.command0_0);
//		v.setOnClickListener(new OnClickListener() {
//			public void onClick(View v) {
//				commandDialog.show();
//			}
//		});
	}

	Paint commandsDrawer = new Paint();
	final char[] colors = new char[4];
	final char[] actions = new char[13];
	int getCommandIndex(char command)
	{
		for(int i = 0; i < actions.length; i++)
			if (actions[i] == command) return i;
		return 0;
	}
	int getColorIndex(char color)
	{
		for(int i = 0; i < colors.length; i++)
			if (colors[i] == color) return i;
		return -1;
	}
	int cw, ch;

    int getActualFunctionCount()
    {
        int actualCount = 0;
        for (int i=0; i<puzzle.getFunctionCount(); i++)
        {
            if (puzzle.getFunctionLength(i)>0)
                actualCount++;
        }
        return actualCount;
    }
	void drawCommands() {
		Bitmap commandsBitmap = getCommandsBitmap();
		final int cellSize = commandsBitmap.getHeight() / 4;
		final int paintCount = puzzle.allowedPaints();
		
		cw = 2 /* empty */ + 3 /* f, l, r */ + getActualFunctionCount() + paintCount;
		ch = Math.max(paintCount + 1, puzzle.colorCount() + 1);
		initColors();
		initActions();
		currentCommands = Bitmap.createBitmap(cellSize * cw, cellSize * ch,
			thumbnailMemoryUsage == ThumbnailMemoryUsage.Normal ? Config.ARGB_8888: Config.RGB_565);
		
		Canvas canvas = new Canvas(currentCommands);
		Rect src = new Rect();
		Rect dst = new Rect();

        //Draw empty spaces
		src.bottom = src.right = dst.bottom = dst.right = cellSize;
		canvas.drawBitmap(commandsBitmap, src, dst, commandsDrawer);
		dst.left = cellSize * (cw - 1);
		dst.right = cellSize * cw;
		dst.bottom = cellSize;
		canvas.drawBitmap(commandsBitmap, src, dst, commandsDrawer);

        //Draw Move arrows and Func1
		src.right = dst.right = cellSize * 4;
		src.left = dst.left = cellSize;
		if (ch == 4){
			src.bottom = dst.bottom = cellSize * 4;
			canvas.drawBitmap(commandsBitmap, src, dst, commandsDrawer);
		} else {
			drawColors(commandsBitmap, cellSize, canvas, src, dst);
		}

        drawFunctionCalls(commandsBitmap, cellSize, canvas, src, dst);
		
		src.left = 9 * cellSize;
		src.right = src.left + cellSize;
		drawPaint('R', commandsBitmap, cellSize, canvas, src, dst);
		drawPaint('G', commandsBitmap, cellSize, canvas, src, dst);
		drawPaint('B', commandsBitmap, cellSize, canvas, src, dst);
	}

    private void drawFunctionCalls(Bitmap commandsBitmap, int cellSize, Canvas canvas, Rect src, Rect dst) {

        src.left = dst.left = cellSize * 4;

        src.bottom = dst.bottom = cellSize * 4;
        for (int i=0; i<puzzle.getFunctionCount(); i++)
        {
            src.top = dst.top = 0;
            src.right = src.left + cellSize;
            dst.right = dst.left + cellSize;

            if (puzzle.getFunctionLength(i)>0)
            {
                if (ch == 4){
                    canvas.drawBitmap(commandsBitmap, src, dst, commandsDrawer);
                } else {
                    drawColors(commandsBitmap, cellSize, canvas, src, dst);
                }
                dst.left = dst.right;
            }
            src.left = src.right;
        }
    }

    void initActions() {
		int cur = 5;
		for(int f = 2; f <= puzzle.getFunctionCount(); f++)
        {
            if (puzzle.getFunctionLength(f-1)>0)
                actions[cur++] = (char)(f + '0');
        }

		if (puzzle.allowedPaint('R')) actions[cur++] = 'r';
		if (puzzle.allowedPaint('G')) actions[cur++] = 'g';
		if (puzzle.allowedPaint('B')) actions[cur++] = 'b';
		actions[cur] = '\0';
	}

	void initColors() {
		if (ch == 4) { colors[1] = 'r'; colors[2] = 'g'; colors[3] = 'b'; }
		else {
			int c = 1;
			if (puzzle.hasColor('R')) colors[c++] = 'r';
			if (puzzle.hasColor('G')) colors[c++] = 'g';
			if (puzzle.hasColor('B')) colors[c++] = 'b';
		}
	}

	private void drawPaint(char color, Bitmap commandsBitmap, int cellSize,
			Canvas canvas, Rect src, Rect dst) {
		src.top = dst.top = 0;
		
		if (puzzle.allowedPaint(color)) {
			dst.left = dst.right;
			dst.right = dst.left + cellSize;
			
			drawColors(commandsBitmap, cellSize, canvas, src, dst);
		}
		
		src.left += cellSize;
		src.right += cellSize;
	}

	void drawColors(Bitmap commandsBitmap, final int cellSize, Canvas canvas,
			Rect src, Rect dst) {
		src.bottom = dst.bottom = cellSize;
		canvas.drawBitmap(commandsBitmap, src, dst, commandsDrawer);
		
		src.top += cellSize;
		src.bottom += cellSize;
		if (puzzle.hasColor('R')){
			dst.top += cellSize;
			dst.bottom += cellSize;
			canvas.drawBitmap(commandsBitmap, src, dst, commandsDrawer);
		}
		src.top += cellSize;
		src.bottom += cellSize;
		if (puzzle.hasColor('G')){
			dst.top += cellSize;
			dst.bottom += cellSize;
			canvas.drawBitmap(commandsBitmap, src, dst, commandsDrawer);
		}
		src.top += cellSize;
		src.bottom += cellSize;
		if (puzzle.hasColor('B')){
			dst.top += cellSize;
			dst.bottom += cellSize;
			canvas.drawBitmap(commandsBitmap, src, dst, commandsDrawer);
		}
	}

	static int NormalSpeedStepTime = 150;
	static final int InfiniteSpeedStepTime = 1;
	
	void stop() {
		stackText.setVisibility(View.INVISIBLE);
		stackText.setText("__");
		editControls.setVisibility(View.VISIBLE);
		speedButtonContainer.setVisibility(View.GONE);

		if (isRunning()){
			try {
				this.sessionLogWriter.LogPlayEnd(state.getTotalSteps());
			}catch (IllegalStateException e){
				Log.w("RobAI", "An attempt to log play end command when robot is not running according to log");
			}
		}

		try{
			commandCells[prevFunction][prevInstruction].setBackgroundColor(Color.TRANSPARENT);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException(
					"Invalid IP " + prevFunction + "," + prevInstruction + " on\n" +
							"Puzzle: " + puzzle.title + "; Program: " + program.getProgram() + "\n" +
							"Robot position: " + state.getX() + "," + state.getY() + "; direction: " + state.getDirection());
		}

		frames = 1;
		finish = GameEnd.No;

		state.reset(puzzle);

		this.storePrevious();
		
		drawPuzzle();
		drawBreakpoints(canvas, 40, breakpoints);
		flush();
		
		pauseButton.setVisibility(View.GONE);
		stopButton.setEnabled(false);
		stopButton.setBackgroundResource(R.drawable.ic_media_stop);
		stepButton.setEnabled(true);
		stepButton.setBackgroundResource(android.R.drawable.ic_media_next);
		
		setProgramEditingAllowed(true);
	}

	private void programChanged() throws SQLException {
		redoButton.setEnabled(false);
		redoButton.setBackgroundResource(android.R.drawable.ic_menu_revert);

		getHelper().getPuzzlesDAO().update(puzzle);
		Dao<EditHistoryItem, Integer> historyDAO = getHelper().getHistoryDAO();
		
		DeleteBuilder<EditHistoryItem, Integer> query = historyDAO.deleteBuilder();
		query.where().eq("puzzle_id", puzzle.id)
			.and().gt("id", currentHistoryItem);
		historyDAO.delete(query.prepare());
		
		EditHistoryItem item = new EditHistoryItem();
		item.program = program.getProgram();
		item.puzzle = puzzle;
		historyDAO.create(item);
		
		currentHistoryItem = item.id;
		
		undoButton.setEnabled(true);
		undoButton.setBackgroundResource(android.R.drawable.ic_menu_revert);
	}

	private void undo() {
		Dao<EditHistoryItem, Integer> historyDAO = getHelper().getHistoryDAO();
		QueryBuilder<EditHistoryItem, Integer> history = historyDAO.queryBuilder();
		try {
			history.where().lt("id", currentHistoryItem)
				.and().eq("puzzle_id", puzzle.id);
			List<EditHistoryItem> items = historyDAO.query(history.prepare());
			EditHistoryItem max = null, other = null;
			for(EditHistoryItem item: items){
				if (max == null) max = item;
				else if (item.id > max.id){
					other = max;
					max = item;
				} else if (other == null || item.id > other.id){
					other = item;
				}
			}
			if (other != null){
				undoButton.setEnabled(true);
				undoButton.setBackgroundResource(android.R.drawable.ic_menu_revert);
			} else{
				undoButton.setEnabled(false);
				undoButton.setBackgroundResource(android.R.drawable.ic_menu_revert);
			}
			if (max != null){
				redoButton.setEnabled(true);
				redoButton.setBackgroundResource(android.R.drawable.ic_menu_revert);
			} else {
				redoButton.setEnabled(false);
				redoButton.setBackgroundResource(android.R.drawable.ic_menu_revert);
			}
			if (max == null) return;
			program.setProgram(max.program);
			currentHistoryItem = max.id;
			initProgram();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	private void redo() {
		Dao<EditHistoryItem, Integer> historyDAO = getHelper().getHistoryDAO();
		QueryBuilder<EditHistoryItem, Integer> history = historyDAO.queryBuilder();
		try {
			history.where().gt("id", currentHistoryItem)
				.and().eq("puzzle_id", puzzle.id);
			List<EditHistoryItem> items = historyDAO.query(history.prepare());
			EditHistoryItem min = null, other = null;
			for(EditHistoryItem item: items){
				if (min == null) min = item;
				else if (item.id < min.id){
					other = min;
					min = item;
				} else if (other == null || item.id < other.id){
					other = item;
				}
			}
			if (min != null){
				undoButton.setEnabled(true);
				undoButton.setBackgroundResource(android.R.drawable.ic_menu_revert);
			} else{
				undoButton.setEnabled(false);
				undoButton.setBackgroundResource(android.R.drawable.ic_menu_revert);
			}
			if (other != null){
				redoButton.setEnabled(true);
				redoButton.setBackgroundResource(android.R.drawable.ic_menu_revert);
			} else{
				redoButton.setEnabled(false);
				redoButton.setBackgroundResource(android.R.drawable.ic_menu_revert);
			}
			if (min == null) return;
			program.setProgram(min.program);
			currentHistoryItem = min.id;
			initProgram();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void setProgramEditingAllowed(boolean enable) {
		programInput.setClickable(enable);
		undoButton.setClickable(enable);
		redoButton.setClickable(enable);
	}

	private void pause() {
		playButton.setVisibility(View.VISIBLE);
		pauseButton.setVisibility(View.GONE);
		stepButton.setEnabled(true);
		stepButton.setBackgroundResource(android.R.drawable.ic_media_next);
	}

	private void run() {
		if (!stopButton.isEnabled()) {
			state.reset(puzzle);
			commandCells[0][0].setBackgroundColor(Color.WHITE);
		}

		if (program.isEmpty()){
			Toast.makeText(this, R.string.emptyProgram, Toast.LENGTH_LONG).show();
			return;
		}
		
		playButton.setVisibility(View.GONE);
		pauseButton.setVisibility(View.VISIBLE);
		stopButton.setEnabled(true);
		stopButton.setBackgroundResource(R.drawable.ic_media_stop);
		stepButton.setEnabled(false);
		stepButton.setBackgroundResource(android.R.drawable.ic_media_next);
		
		setProgramEditingAllowed(false);

		createStepper();
		stepper.execute();
	}

	private void setSpeed(int speed){
		// 0 <= speed <= 100
		// 0 => 3000
		// 50 => 150
		// 100 => 1
		NormalSpeedStepTime = Math.max((int)(1000.41*Math.exp(-0.0387274*speed)), 1);
		Editor edit = pref.edit();
		edit.putInt(RoboZZleSettings.SPEED, speed);
		edit.commit();
	}

	MenuItem load;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.solver, menu);
	    MenuItem sync = menu.findItem(R.id.menuHelp);
	    sync.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				helpDialog.show();
				return true;
			}
		});
	    
	    MenuItem clear = menu.findItem(R.id.menuClearCode);
	    clear.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				clearCode();
				return true;
			}
		});

		MenuItem details = menu.findItem(R.id.menuDetails);
		details.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem menuItem) {
				if (puzzle != null) openPuzzle(puzzle.id, PuzzleDetails.class);
				return true;
			}
		});
	    
	    load = menu.findItem(R.id.menuLoadSolution);
	    load.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				loadSolution();
				return true;
			}
		});
	    if (puzzle != null) load.setEnabled(puzzle.solution != null); 
	    
	    
	    return true;
	}
	
	boolean checkRunning(){
		if (stopButton.isEnabled()){
			Toast.makeText(Solver.this, R.string.cantModifyWhileRunning, Toast.LENGTH_SHORT).show();
			return true;
		}
		
		return false;
	}
	
	protected void loadSolution() {
		if (checkRunning()) return;
		
		if (puzzle.solution == null || puzzle.solution.length() == 0 || puzzle.solution.equals("|||||")) {
			Toast.makeText(this, R.string.noSolution, Toast.LENGTH_SHORT).show();
			return;
		}
		
		puzzle.setProgram(puzzle.solution);
		onProgramChanged();
	}

	protected void clearCode() {
		if (checkRunning()) return;
		
		puzzle.setProgram("");
		this.sessionLogWriter.Log(ClearCommand.getInstance());
		onProgramChanged();
	}
}
