package com.team242.robozzle.achievements;

import com.team242.robozzle.model.Puzzle;

/**
 * Created by IntelliJ IDEA.
 * User: lost
 * Date: 11.11.11
 * Time: 19:58
 * To change this template use File | Settings | File Templates.
 */
public class AuthorPuzzlesAchievement extends PuzzleCountAchievement {
	final String author;

	public AuthorPuzzlesAchievement(int titleID, int descriptionID, int iconID, int count, String author){
		super(titleID, descriptionID, iconID, count);

		if (author == null) throw new IllegalArgumentException();
		this.author = author;
	}

	protected boolean puzzleMatches(Puzzle puzzle){
		if (puzzle == null) return false;
		return author.equals(puzzle.submittedBy);
	}
}
