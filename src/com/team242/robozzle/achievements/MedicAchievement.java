package com.team242.robozzle.achievements;

import com.team242.robozzle.model.Puzzle;
import com.team242.robozzle.R;

/**
 * Created by IntelliJ IDEA.
 * User: lost
 * Date: 11.11.11
 * Time: 20:30
 */
public class MedicAchievement extends PuzzleCountAchievement {
	private MedicAchievement(){
		super(R.string.medicTitle, R.string.medicDescription, R.drawable.medic, 0);
	}

	public boolean puzzleMatches(Puzzle puzzle){
		if (!super.puzzleMatches(puzzle)) return false;

		int centerX = -1, centerY = -1;
		for(int x = 1; x + 1 < puzzle.getWidth(); x++){
			for(int y = 1; y + 1 < puzzle.getHeight(); y++){
				if (puzzle.isBlack(x, y)) continue;
				if (puzzle.isBlack(x-1,y)) continue;
				if (puzzle.isBlack(x+1,y)) continue;
				if (puzzle.isBlack(x,y-1)) continue;
				if (puzzle.isBlack(x,y+1)) continue;
				centerX = x;
				centerY = y;
				break;
			}

			if (centerX >= 0) break;
		}

		if (centerX < 0) return false;

		for(int x = 0; x < puzzle.getWidth(); x++){
			if (x == centerX) continue;
			for(int y = 0; y < puzzle.getHeight(); y++){
				if (y == centerY) continue;
				if (!puzzle.isBlack(x, y)) return false;
			}
		}

		return true;
	}
	
	public static final Achievement Instance = new MedicAchievement();
}
