package com.team242.robozzle.achievements;

import android.content.SharedPreferences;
import com.team242.robozzle.Achievements;
import com.team242.robozzle.model.Puzzle;

/**
 * Created by IntelliJ IDEA.
 * User: lost
 * Date: 11.11.11
 * Time: 19:40
 */
public abstract class Achievement {
	public final int titleID;
	public final int descriptionID;
	public final int iconID;

	public Achievement(int titleID, int descriptionID, int iconID){
		this.titleID = titleID;
		this.descriptionID = descriptionID;
		this.iconID = iconID;
	}

	public abstract boolean isDone(Puzzle[] puzzles);
	
	protected String getName(){
		for(int i = 0; i < Achievements.achievements.length; i++){
			if (Achievements.achievements[i] == this)
				return "achievement" + i;
		}
		
		assert false;
		return null;
	}
	
	public boolean isStateSolved(SharedPreferences preferences){
		String name = getName();
		return preferences.getBoolean(name, false);
	}
	
	public void setState(SharedPreferences preferences, boolean solved){
		String name = getName();

		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(name,  solved);
		
		editor.commit();
	}

}
