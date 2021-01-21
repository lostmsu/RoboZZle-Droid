/**
 * 
 */
package com.team242.robozzle;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.view.*;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.team242.robozzle.achievements.Achievement;
import com.team242.robozzle.achievements.BraveHeartAchievement;
import com.team242.robozzle.achievements.CowardAchievement;
import com.team242.robozzle.model.Puzzle;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

/**
 * @author lost
 *
 */
public class UnsolvedPuzzles extends GenericPuzzleActivity {
	private PuzzleAdapter adapter;
	
	ListView puzzles;

	@Override
	protected void onDestroy() {
		if (adapter != null)
			adapter.cancel();

		super.onDestroy();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.puzzles);

		lowMemoryCheck();
		
		puzzles = (ListView)findViewById(R.id.puzzles);
		Toolbar toolbar = (Toolbar)findViewById(R.id.puzzlesToolbar);
		this.setSupportActionBar(toolbar);
		adapter = new PuzzleAdapter(this);
		boolean showSolved = pref.getBoolean(RoboZZleSettings.SHOW_SOLVED, true);
		boolean hideUnpopular = pref.getBoolean(RoboZZleSettings.HIDE_UNPOPULAR, true);
		if (!pref.contains(RoboZZleSettings.ROB_AI_TELEMETRY_ENABLED)){
			this.showResearchSuggestionDialog();
		} else if (!loggedIn()){
			Toast.makeText(this, R.string.must_sign_in, Toast.LENGTH_LONG).show();
			this.showLoginDialog();
		}
		adapter.setShowSolved(showSolved);
		adapter.setHideUnpopular(hideUnpopular);
		
		try {
			if (adapter.getTutorialCount() == 0)
				adapter.createInitialPuzzles();

			if (adapter.getCount() <= adapter.getTutorialCount()) {
				adapter.updatePuzzles((LinearLayout)findViewById(R.id.syncPane), RobozzleWebClient.SortKind.POPULAR);
			}
		} catch (SQLException e) {
			// TODO: report exception
			throw new RuntimeException(e);
		}
		
		puzzles.setAdapter(adapter);
		puzzles.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> view, View baseView, int itemIndex,
			                        long itemID) {
				openPuzzle((int) itemID);
			}
		});

	}

	private void showResearchSuggestionDialog() {
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.research_participation_dialog);
		dialog.setTitle(R.string.researchDialogTitle);

		View acceptButton = dialog.findViewById(R.id.accept);
		View declineButton = dialog.findViewById(R.id.decline);
		acceptButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Editor prefEditor = pref.edit();
				prefEditor.putBoolean(RoboZZleSettings.ROB_AI_TELEMETRY_ENABLED, true);
				prefEditor.putBoolean(RoboZZleSettings.AUTO_SOLUTION_SUBMIT, true);
				prefEditor.commit();
				if (!loggedIn())
					showLoginDialog();
				dialog.dismiss();
			}
		});
		declineButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Editor prefEditor = pref.edit();
				prefEditor.putBoolean(RoboZZleSettings.ROB_AI_TELEMETRY_ENABLED, false);
				prefEditor.commit();
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private void showLoginDialog() {
		LoginDialog loginDialog = new LoginDialog(this);
		loginDialog.pref = this.pref;
		loginDialog.show();
	}

	private static void preallocate(){
		boolean oom = false;
		int size = 128*1024;
		while (!oom){
			try{
				byte[] block = new byte[size];
				size *= 2;
				block[0] = 1;
				block[block.length - 1] = -1;
			}catch (OutOfMemoryError error){
				oom = true;
			}
		}
	}

	private void lowMemoryCheck() {
		if (pref.contains(RoboZZleSettings.THUMBNAIL_MEMORY_USAGE)){
			switch (Integer.parseInt(pref.getString(RoboZZleSettings.THUMBNAIL_MEMORY_USAGE, "0"))){
			case 0:
				thumbnailMemoryUsage = ThumbnailMemoryUsage.Normal;
				break;
			case 1:
				thumbnailMemoryUsage = ThumbnailMemoryUsage.Limited;
				break;
			case -1:
				thumbnailMemoryUsage = ThumbnailMemoryUsage.Disabled;
				break;
			}
		} else {
			preallocate();

			Runtime runtime = Runtime.getRuntime();
			Editor editor = pref.edit();
			if (runtime.maxMemory() <= 16*1024*1024){
				thumbnailMemoryUsage = ThumbnailMemoryUsage.Limited;
				editor.putString(RoboZZleSettings.THUMBNAIL_MEMORY_USAGE, "1");
				Toast.makeText(this, R.string.lowMemMode, Toast.LENGTH_LONG).show();
			} else{
				thumbnailMemoryUsage = ThumbnailMemoryUsage.Normal;
				editor.putString(RoboZZleSettings.THUMBNAIL_MEMORY_USAGE, "0");
			}
			editor.commit();
		}
	}

	private void openPuzzle(int puzzleID) {
		openPuzzle(puzzleID, Solver.class);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (intent == null) return;
		if (intent.hasExtra(Intents.PUZZLE_SOLVED)){
			int puzzleID = intent.getIntExtra(Intents.PUZZLE_SOLVED, 0);
			
			final Puzzle puzzle = refreshPuzzle(puzzleID);

			checkAchievements();
			
			if (puzzleID < 0) return;
			if (!pref.getBoolean(RoboZZleSettings.AUTO_SOLUTION_SUBMIT, true)) return;
			
			submitSolution(puzzle);
		}
		if (intent.hasExtra(Intents.PUZZLE_SCARY)) {
			int puzzleID = intent.getIntExtra(Intents.PUZZLE_SCARY, 0);

			refreshPuzzle(puzzleID);

			checkAchievements(new Achievement[]{CowardAchievement.Instance, BraveHeartAchievement.Instance});
		}
	}

	private Puzzle refreshPuzzle(int puzzleID){
		int childCount = puzzles.getChildCount();
		View view;
		final Puzzle puzzle = getPuzzle(puzzleID);
		for(int i = 0; i < childCount; i++){
			view = puzzles.getChildAt(i);
			if (view == null) continue;
			Puzzle viewPuzzle = (Puzzle)view.getTag();
			if (viewPuzzle.id == puzzleID) {
				adapter.setPuzzle(view, puzzle);
				adapter.refresh(puzzle);
				break;
			}
		}
		return puzzle;
	}

	private void submitSolution(final Puzzle puzzle) {
		final String login = pref.getString(RoboZZleSettings.LOGIN, "");
		final String password = pref.getString(RoboZZleSettings.PASSWORD, "");
		
		if (login.equals("") || password.equals("")){
			Toast.makeText(this, R.string.provideLogin, Toast.LENGTH_LONG);
			return;
		}
		
		AsyncTask<Void, Void, Void> submitter = new AsyncTask<Void, Void, Void>() {
			int message = R.string.solutionsSynchronized;
			
			@Override
			protected void onPostExecute(Void result) {
				if (isCancelled()) return;
				
				Toast.makeText(UnsolvedPuzzles.this, message, Toast.LENGTH_LONG).show();
			}
			
			@Override
			protected Void doInBackground(Void... params) {
				RobozzleWebClient webClient = new RobozzleWebClient();
				try{
					String solution = puzzle.getSolutionProgram().getProgram().replace("__", "");
					String result = webClient.SubmitSolution(puzzle.id, login, password, solution);
					
					if (result != null){
						Exception error = new InvalidAlgorithmParameterException("Solution " + solution + 
								" does not fits puzzle " + puzzle.title + " according to RoboZZle.com");
						// TODO: report exception
					}
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.puzzle_list_menu, menu);
	    MenuItem update = menu.findItem(R.id.updatePuzzles);
	    update.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				updatePuzzles();
				return true;
			}
		});
	    
	    MenuItem share = menu.findItem(R.id.shareSolutions);
	    share.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				shareSolutions();
				return true;
			}
		});
	    
	    MenuItem showSolved = menu.findItem(R.id.filterSolved);
	    showSolved.setIcon(pref.getBoolean(RoboZZleSettings.SHOW_SOLVED, true)
	    		? android.R.drawable.presence_online
	    		: android.R.drawable.presence_invisible);
	    showSolved.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				boolean showSolved = pref.getBoolean(RoboZZleSettings.SHOW_SOLVED, true);
				showSolved = !showSolved;
				Editor edior = pref.edit();
				edior.putBoolean(RoboZZleSettings.SHOW_SOLVED, showSolved);
				item.setIcon(showSolved? android.R.drawable.presence_online: android.R.drawable.presence_invisible);
				adapter.setShowSolved(showSolved);
				edior.commit();
				return true;
			}
		});
	    MenuItem hideUnpopular = menu.findItem(R.id.filterPopular);
	    hideUnpopular.setIcon(pref.getBoolean(RoboZZleSettings.HIDE_UNPOPULAR, true)
	    		? android.R.drawable.star_on
	    		: android.R.drawable.star_off);
	    hideUnpopular.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				boolean hideUnpopular = pref.getBoolean(RoboZZleSettings.HIDE_UNPOPULAR, true);
				hideUnpopular = !hideUnpopular;
				Editor editor = pref.edit();
				editor.putBoolean(RoboZZleSettings.HIDE_UNPOPULAR, hideUnpopular);
				item.setIcon(hideUnpopular? android.R.drawable.star_on: android.R.drawable.star_off);
				adapter.setHideUnpopular(hideUnpopular);
				editor.commit();
				return true;
			}
		});
	    
	    MenuItem showSettings = menu.findItem(R.id.showSettings);
	    showSettings.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				showSetting();
				return true;
			}
		});
		
		MenuItem jumpTo = menu.findItem(R.id.openCustomPuzzle);
		jumpTo.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				openCustomPuzzle();
				return true;
			}
		});

        MenuItem recheckAchievements = menu.findItem(R.id.checkAchievements);
        recheckAchievements.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                openAchievements();
                return true;
            }
        });

		MenuItem about = menu.findItem(R.id.about);
		about.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				showAbout();
				return true;
			}
		});

		MenuItem search = menu.findItem(R.id.action_search);
		try {
			SearchView searchView = (SearchView)search.getActionView();
			searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
				@Override
				public boolean onQueryTextSubmit(String query) {
					adapter.setQueryString(query);
					return true;
				}

				@Override
				public boolean onQueryTextChange(String newText) {
					adapter.setQueryString(newText);
					return true;
				}
			});
			searchView.setOnCloseListener(new SearchView.OnCloseListener() {
				@Override
				public boolean onClose() {
					adapter.setQueryString(null);
					return false;
				}
			});
		}catch (NoSuchMethodError e){
			search.setEnabled(false);
		}
		
	    return true;

	}

	private void showAbout(){
		Intent i = new Intent(UnsolvedPuzzles.this, AboutActivity.class);
		startActivity(i);
	}

	private void showSetting() {
		Intent i = new Intent(UnsolvedPuzzles.this, RoboZZleSettings.class);
		startActivity(i);
	}

	private void shareSolutions() {
		String login = pref.getString(RoboZZleSettings.LOGIN, "");
		String password = pref.getString(RoboZZleSettings.PASSWORD, "");
		if (login.length() == 0 || password.length() == 0) {
			Toast.makeText(this, R.string.provideLogin, Toast.LENGTH_LONG).show();
			return;
		}
		
		if (adapter.isSynchronizing()){
			Toast.makeText(this, R.string.alreadySynchronizing, Toast.LENGTH_SHORT).show();
			return;
		}
		
		adapter.shareSolutions();
	}
	
	private void updatePuzzles() {
		if (adapter.isSynchronizing()){
			Toast.makeText(this, R.string.alreadySynchronizing, Toast.LENGTH_SHORT).show();
			return;
		}
		
		View syncPane = findViewById(R.id.syncPane);
		boolean hideUnpopular = pref.getBoolean(RoboZZleSettings.HIDE_UNPOPULAR, true);
		RobozzleWebClient.SortKind sortKind = hideUnpopular
				? RobozzleWebClient.SortKind.POPULAR
				: RobozzleWebClient.SortKind.DESCENDING_ID;
		adapter.updatePuzzles((LinearLayout)syncPane, sortKind);
	}

	private void openCustomPuzzle(){
		final EditText alertInput = new EditText(this);
		alertInput.setInputType(InputType.TYPE_CLASS_NUMBER);

		AlertDialog.Builder alert =
			new AlertDialog.Builder(this)
				.setTitle(R.string.jump_to_puzzle)
				.setMessage(R.string.prompt_puzzle_id)
				.setView(alertInput)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						try {
							int puzzleID = Integer.parseInt(alertInput.getText().toString());
							Puzzle puzzle = getHelper().getPuzzlesDAO().queryForId(puzzleID);
							if (puzzle == null)
								Toast.makeText(UnsolvedPuzzles.this,
										R.string.invalidPuzzleID,
										Toast.LENGTH_LONG).show();
							else
								openPuzzle(puzzleID);
						} catch (NumberFormatException e) {
							Toast.makeText(UnsolvedPuzzles.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
						} catch (SQLException e){
							e.printStackTrace();
							throw new RuntimeException(e);
						}
					}
				})
				.setNegativeButton(R.string.cancel, null);

		AlertDialog dialog = alert.create();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.show();
	}
}
