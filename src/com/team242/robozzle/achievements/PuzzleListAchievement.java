package com.team242.robozzle.achievements;

import com.team242.robozzle.model.Puzzle;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: lost
 * Date: 11.11.11
 * Time: 19:49
 */
public class PuzzleListAchievement extends PuzzleCountAchievement{
	final Set<Integer> puzzles;

	public PuzzleListAchievement(int titleID, int descriptionID, int iconID, int[] puzzles){
		super(titleID, descriptionID, iconID, 0);

		if (puzzles == null) throw new IllegalArgumentException();
		this.puzzles = new HashSet<Integer>();
		for(int puzzleID: puzzles) this.puzzles.add(puzzleID);
	}

	public boolean puzzleMatches(Puzzle puzzle){
		return puzzle != null && puzzles.contains(puzzle.id);
	}
	
	@Override
	public boolean isDone(Puzzle[] puzzles){
		int found = 0;
		for(Puzzle puzzle: puzzles){
			if (this.puzzles.contains(puzzle.id)) found++;
		}
		
		if (found < this.puzzles.size()) return false;
		
		return super.isDone(puzzles);
	}
}
