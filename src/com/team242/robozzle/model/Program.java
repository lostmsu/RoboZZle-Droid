/**
 * 
 */
package com.team242.robozzle.model;

import com.team242.util.Array;

/**
 * @author lost
 *
 */
public class Program {
	public Program(Puzzle puzzle){
		colors = new char[puzzle.getFunctionCount()][];
		actions = new char[puzzle.getFunctionCount()][];
		for(int i = 0; i < colors.length; i++){
			colors[i] = new char[puzzle.getFunctionLength(i)];
			actions[i] = new char[puzzle.getFunctionLength(i)];
		}
	}
	
	public Program(Puzzle puzzle, String program){
		this(puzzle);
		setProgram(program);
	}
	
	public void setProgram(String program){
		Array.clear(colors);
		Array.clear(actions);
		
		if (program == null) return;
		if (program.length() == 0) return;
		
		int func = 0, instr = 0;
		for(int i = 0; i < program.length(); ){
			char color = program.charAt(i);
			if (color == '|'){
				instr = 0;
				func++;
				i++;
			} else {
				Exception error = null;
				if (func >= colors.length) {
					error = new IllegalStateException("Too many functions in " + program + ". Expected: " + colors.length);
				} else if (instr >= colors[func].length)
					error = new IllegalStateException("Too many instructions in function " + func +" in " + program + ". Expected: " + colors[func].length);
				if (error != null) {
					// TODO: report exception
					return;
				}
				if (Character.isDigit(color) || Character.isUpperCase(color)){
					colors[func][instr] = '_';
					actions[func][instr] = color;
					i++;
				} else{
					colors[func][instr] = color;
					actions[func][instr] = program.charAt(i + 1);
					if (actions[func][instr] == '|') {
						actions[func][instr] = '_';
						i++;
					}else i += 2;
				}
				instr++;
			}
		}
	}
	
	public void setInstruction(int func, int instr, char color, char action){
		colors[func][instr] = color;
		actions[func][instr] = action;
	}
	
	public String getProgram(){
		StringBuilder result = new StringBuilder();
		for(int func = 0; func < 5; func++){
			if (func >= colors.length){
				result.append('|');
				continue;
			}
			
			for(int instr = 0; instr < colors[func].length; instr++){
				if (actions[func][instr] == '\0' || actions[func][instr] == '_') {
					actions[func][instr] = '_';
					colors[func][instr] = '_';
				}
				if (colors[func][instr] == '\0') colors[func][instr] = '_';
				char color = colors[func][instr];
				result.append(color);
				result.append(actions[func][instr]);
			}
			
			result.append('|');
		}
		
		return result.toString();
	}

	public boolean isEmpty(){
		String code = getProgram();
		for(int i = 0; i < code.length(); i++){
			char c = code.charAt(i);
			if (c != '_' && c != '\0' && c != '|') return false;
		}
		return true;
	}
	
	public char[][] colors;
	public char[][] actions;
	
	public int getFunctionLength(int func){
		return colors[func].length;
	}
}
