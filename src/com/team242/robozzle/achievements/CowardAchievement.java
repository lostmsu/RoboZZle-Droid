package com.team242.robozzle.achievements;

import com.team242.robozzle.model.Puzzle;
import com.team242.robozzle.R;

/**
 * Created by IntelliJ IDEA.
 * User: lost
 * Date: 11.11.11
 * Time: 19:52
 */
public class CowardAchievement extends Achievement {
	private CowardAchievement() {
		super(R.string.cowardTitle, R.string.cowardDescription, R.drawable.coward);
	}
 
    @Override
    public boolean isDone(Puzzle[] puzzles){
        for(Puzzle puzzle: puzzles){
            if (puzzle.getScary() > 0) return true;
        }
        return false;
    }
	
	public static final Achievement Instance = new CowardAchievement();
}
