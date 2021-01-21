package com.team242.robozzle.achievements;

import com.team242.robozzle.model.Puzzle;

/**
 * Created by IntelliJ IDEA.
 * User: lost
 * Date: 11.11.11
 * Time: 20:14
 */
public class DifficultyRangeCountAchievement extends PuzzleCountAchievement {
	final int min, max;

	/**
	 * @param titleID Achievement title ID in strings.xml
	 * @param descriptionID Achievement description ID in strings.xml
	 * @param iconID Achievement icon ID
	 * @param count Required number of solved puzzles. Use 0 for all matching puzzles,
	 *              and -n for solving at least 1/n of popular puzzles.
	 * @param min Low difficulty bound (inclusive)
	 * @param max High difficulty bound (inclusive)
	 */
	public DifficultyRangeCountAchievement(int titleID, int descriptionID, int iconID, int count, int min, int max){
		super(titleID, descriptionID, iconID, count);

		if (min < 0 || max > 100 || max < min) throw new IllegalArgumentException();
		this.max = max;
		this.min = min;
	}

	protected boolean puzzleMatches(Puzzle puzzle){
		return super.puzzleMatches(puzzle) && puzzle.difficulty >= min && puzzle.difficulty <= max;
	}
}
