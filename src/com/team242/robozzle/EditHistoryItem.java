/**
 * 
 */
package com.team242.robozzle;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.team242.robozzle.model.Puzzle;

/**
 * @author lost
 *
 */
@DatabaseTable(tableName = "EDIT_HISTORY")
public class EditHistoryItem {
	public EditHistoryItem(){
		
	}
	
	@DatabaseField(generatedId = true)
	public int id;
	
	@DatabaseField(canBeNull = false, foreign = true)
	public Puzzle puzzle;
	
	@DatabaseField(canBeNull = false)
	public String program;
}
