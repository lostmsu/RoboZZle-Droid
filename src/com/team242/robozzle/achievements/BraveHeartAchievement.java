package com.team242.robozzle.achievements;

import com.team242.robozzle.model.Puzzle;
import com.team242.robozzle.R;

/**
 * Created by IntelliJ IDEA.
 * User: lost
 * Date: 11.11.11
 * Time: 19:53
 */
public class BraveHeartAchievement extends PuzzleCountAchievement {
	private BraveHeartAchievement(){
		super(R.string.braveTitle, R.string.braveDescription, R.drawable.braveheart, 0);
	}

	public boolean puzzleMatches(Puzzle puzzle){
		return puzzle != null && puzzle.getScary() > 0;
	}
	
	public static final Achievement Instance = new BraveHeartAchievement();

    @Override
    protected boolean isDone(AchievementStatistics statistics){
        if (statistics.matching <= 0)
            return false;

        return super.isDone(statistics);
    }
}
