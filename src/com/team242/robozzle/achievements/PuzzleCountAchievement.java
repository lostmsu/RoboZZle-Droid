package com.team242.robozzle.achievements;

import com.team242.robozzle.model.Puzzle;

/**
 * Created by IntelliJ IDEA.
 * User: lost
 * Date: 11.11.11
 * Time: 19:55
 */
public class PuzzleCountAchievement extends Achievement{
    public static final int ALL_MATCHING = 0;

	final int count;

	/**
	 *
	 * @param titleID Achievement title ID in strings.xml
	 * @param descriptionID Achievement description ID in strings.xml
	 * @param iconID Achievement icon ID
	 * @param count Required number of solved puzzles. Use 0 for all matching puzzles,
	 *              and -n for solving at least 1/n of popular puzzles.
	 */
	public PuzzleCountAchievement(int titleID, int descriptionID, int iconID, int count){
		super(titleID, descriptionID, iconID);

		this.count = count;
	}

	protected boolean puzzleMatches(Puzzle puzzle){
		if (puzzle == null) return false;
		return puzzle.isPopular() || count >= 0;
	}

    protected AchievementStatistics calculateStatistics(Puzzle[] puzzles){
        AchievementStatistics statistics = new AchievementStatistics();

        for(Puzzle puzzle: puzzles){
            if (!puzzleMatches(puzzle))
                continue;

            statistics.matching++;
            String solution = puzzle.getSolution();
            if (solution != null)
                statistics.solved++;
        }

        return statistics;
    }

    protected boolean isDone(AchievementStatistics statistics){
        if (count > 0)
            return statistics.solved >= this.count;
        else if (count == 0)
            return statistics.solved == statistics.matching;
        else
            return statistics.solved * -count >= statistics.matching;
    }

	@Override
	public boolean isDone(Puzzle[] puzzles){
        AchievementStatistics statistics = calculateStatistics(puzzles);
		
		return isDone(statistics);
	}

    protected static class AchievementStatistics{
        public int solved;
        public int matching;
    }
}
