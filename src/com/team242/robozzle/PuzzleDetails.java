package com.team242.robozzle;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import android.widget.Toast;
import com.team242.robozzle.model.Puzzle;

import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: lost
 * Date: 01.11.12
 * Time: 21:53
 * To change this template use File | Settings | File Templates.
 */
public class PuzzleDetails extends GenericPuzzleActivity {
	Puzzle puzzle;

	TextView subtitle, author, submitDate, forumLink;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.puzzle_details);

		findViews();

		onNewIntent(getIntent());
	}

	private void findViews() {
		subtitle = (TextView)findViewById(R.id.subtitle);
		author = (TextView)findViewById(R.id.author);
		submitDate = (TextView)findViewById(R.id.submitDate);
		forumLink = (TextView)findViewById(R.id.forumLink);
		forumLink.setMovementMethod(LinkMovementMethod.getInstance());
	}

	@Override
	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);

		if (intent.hasExtra(Intents.PUZZLE_EXTRA))
			loadPuzzle(intent.getIntExtra(Intents.PUZZLE_EXTRA, 0));
	}

	private void loadPuzzle(int puzzleID) {
		try{

			try {
				puzzle = getHelper().getPuzzlesDAO().queryForId(puzzleID);
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

			showPuzzleDetails();
		}catch (Exception e){
			Toast.makeText(this, R.string.puzzleLoadFail, Toast.LENGTH_LONG).show();
			Exception puzzleError = new PuzzleException(puzzleID, e);
			// TODO: report exception
			finish();
		}
	}

	private void showPuzzleDetails() {
		Resources res = getResources();

		this.setTitle(puzzle.title);

		if (puzzle.hasDescription())
			subtitle.setText(puzzle.about);
		else
			subtitle.setText(R.string.noDescription);

		author.setText(String.format(res.getString(R.string.authorDetail), puzzle.submittedBy));

		String submitDateString = puzzle.submittedDate != null
			? puzzle.submittedDate.toString()
			: res.getString(R.string.unknown);
        String submitDateFormat = res.getString(R.string.dateDetail);
		submitDate.setText(String.format(submitDateFormat, submitDateString));

		String linkHtml = String.format(res.getString(R.string.forumLink), puzzle.id);
		forumLink.setText(Html.fromHtml(linkHtml));

//				"<a href='http://www.robozzle.com/forums/thread.aspx?puzzle="
//				+ puzzle.id + "'>Discuss puzzle " + puzzle.id + "</a>"
	}
}
