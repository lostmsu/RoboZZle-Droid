/**
 * 
 */
package com.team242.robozzle;

import android.os.Bundle;
import android.widget.ListView;
import com.team242.robozzle.achievements.*;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author lost
 *
 */
public class Achievements extends GenericPuzzleActivity {

	public static final Achievement[] achievements = new Achievement[]{
			// diligent student
			new DifficultyRangeCountAchievement(R.string.diligentStudentTitle, R.string.diligentStudentDescription,
				R.drawable.diligentstudent, -1, 0, 10),

			CowardAchievement.Instance,
			BraveHeartAchievement.Instance,

			// that was easy
			new DifficultyRangeCountAchievement(R.string.thatWasEasyTitle, R.string.thatWasEasyDescription,
					R.drawable.abc, 10, 0, 45),

			BlackBlueAchievement.Instance,
			MedicAchievement.Instance,
            SpirallyAchievement.Instance,

			// igoro
			new AuthorPuzzlesAchievement(R.string.igoroTitle,  R.string.igoroDescription,
					R.drawable.thankyouigoro, -1, "igoro"),

			// magellan
			new PuzzleListAchievement(R.string.magellanTitle,  R.string.magellanDescription,
					R.drawable.magellan, new int[]{36}),

			// clone master
			new PuzzleListAchievement(R.string.cloneMasterTitle, R.string.cloneMasterDescription,
					R.drawable.clonemaster, new int[]{66, 328, 328}),

			// recursion man
			new DifficultyRangeCountAchievement(R.string.recursionManTitle, R.string.recursionDescription,
					R.drawable.recursion, -1, 75, 100),

			// RoboGuru
			new PuzzleCountAchievement(R.string.guruTitle,  R.string.guruDescription,
					R.drawable.roboguru,  -1),

			// RoboNerd
			new PuzzleCountAchievement(R.string.roboNerdTitle, R.string.roboNerdDescription,
					R.drawable.robonerd, 256),

			// P = NP
			new PuzzleCountAchievement(R.string.pnpTitle, R.string.pnpDescription,
					R.drawable.pnp, 0),

			// 42!
			new DifficultyRangeCountAchievement(R.string.answerTitle,  R.string.answerDescription,
					R.drawable.thequestion, 0, 42, 42),

			// half way
			new PuzzleCountAchievement(R.string.halfWayTitle, R.string.halfWayDescription,
					R.drawable.halfway, -1),
		};
	AchievementAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.achievements);

        adapter = new AchievementAdapter(this,
                R.layout.achievement_list_entry,
                new ArrayList<Achievement>(Arrays.asList(achievements)));
        ListView achievementsView = (ListView)findViewById(R.id.achievements);
        achievementsView.setAdapter(adapter);

		/*puzzles.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> view, View baseView, int itemIndex,
					long itemID) {
				openAchievements(Puzzle((int) itemID);
			}
		});  */

	}
}
