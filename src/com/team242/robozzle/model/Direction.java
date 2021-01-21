/**
 * 
 */
package com.team242.robozzle.model;

/**
 * @author lost
 *
 */
public class Direction{
	public static final int RIGHT = 0;
	public static final int TOP = 3;
	public static final int LEFT = 2;
	public static final int DOWN = 1;
	
	public static int TurnLeft(int direction){
		return (direction + 3) & 0x3;
	}
	
	public static int TurnRight(int direction){
		return (direction + 1) & 0x3;
	}
}
