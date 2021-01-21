package com.team242.robozzle.achievements;

import com.team242.robozzle.model.Puzzle;
import com.team242.robozzle.R;

/**
 * Created by IntelliJ IDEA.
 * User: lost
 * Date: 11.11.11
 * Time: 20:24
 */
public class BlackBlueAchievement extends PuzzleCountAchievement {
	private BlackBlueAchievement(){
		super(R.string.blackAndBlueTitle, R.string.blackAndBlueDescription, R.drawable.blueblack, ALL_MATCHING);
	}

	public boolean puzzleMatches(Puzzle puzzle){
		if (!super.puzzleMatches(puzzle))
            return false;

		if (puzzle.colorCount() > 1)
            return false;
		if (!puzzle.hasColor('B'))
            return false;

		return puzzle.allowedPaints() == 0;
	}
	
	public static final Achievement Instance = new BlackBlueAchievement();
}
