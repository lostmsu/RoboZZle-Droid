package com.team242.robozzle.model;

/**
 * Created by lost on 2/21/2016.
 */
public class Command {
	public static String Parse(String command, int offset){
		return command.substring(offset, offset + 2);
	}
}
