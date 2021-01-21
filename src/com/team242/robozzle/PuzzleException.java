package com.team242.robozzle;

/**
 * Created by IntelliJ IDEA.
 * User: lost
 * Date: 28.03.12
 * Time: 17:19
 * To change this template use File | Settings | File Templates.
 */
public class PuzzleException extends Exception {
	public PuzzleException(int puzzleID, Exception innerException){
		super(innerException == null
				? "Unknown error with puzzle "+ puzzleID
				: innerException.getMessage() + " in " + puzzleID);
	}
}
