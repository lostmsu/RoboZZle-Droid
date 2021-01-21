/**
 * 
 */
package com.team242.robozzle.model;

import com.team242.robozzle.model.Puzzle.Point;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * @author lost
 *
 */
public class ProgramState {
	public static class IP{
		public IP(int function, int command){
			this.function = function;
			this.command = command;
		}
		
		public IP(){}
		
		public int function;
		public int command;
		
//		public IP(int function, int command){
//			this.function = function;
//			this.command = command;
//		}
		
		public void Step(){
			command++;
		}
	}
	
	public ProgramState(Puzzle puzzle){
		puzzleID = puzzle.id;
		program = puzzle.getCurrentProgram();
		
		for(int x = 0; x < Puzzle.WIDTH; x++)
			colors[x] = new char[Puzzle.HEIGHT];
		
		reset(puzzle);
	}

	public void reset(Puzzle puzzle) {
		if (puzzleID != puzzle.id){
			Exception error = new IllegalStateException("New puzzle's ID " + puzzle.id + 
					" differs from old one " + puzzleID);
			// TODO: report exception
		}
		puzzleID = puzzle.id;
		
		for(int x = 0; x < Puzzle.WIDTH; x++)
			for(int y = 0; y < Puzzle.HEIGHT; y++)
				colors[x][y] = puzzle.getColor(x, y);
		
		stars.addAll(puzzle.getStars());
		ip = new IP();
		stack.clear();
		direction = puzzle.robotDir;
		x = puzzle.robotCol;
		y = puzzle.robotRow;
		this.totalSteps = this.actionCount = 0;
	}
	
	int puzzleID;
	Program program;
	char[][] colors = new char[Puzzle.WIDTH][];
	private HashSet<Point> stars = new HashSet<Point>();
	private Stack<IP> stack = new Stack<IP>();
	private IP ip;
	private int direction, x, y;
	int totalSteps, actionCount;

	public int getStepsMade(){
		return this.actionCount;
	}

	public int getTotalSteps(){
		return this.totalSteps;
	}
	
	public char getColor(int x, int y){
		return colors[x][y];
	}
	
	public Point getPosition(){
		return new Point(x, y);
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public Set<Point> getStars(){
		return Collections.unmodifiableSet(stars);
	}
	
	public IP getIP(){ return new IP(ip.function, ip.command); }
	
	public int getDirection(){
		return direction;
	}
	
	boolean isBlack(int x, int y){
		return x < 0 || y < 0 || x >= Puzzle.WIDTH || y >= Puzzle.HEIGHT || colors[x][y] == '\0';
	}
	
	public enum GameEnd{
		No,
		Win,
		Fall,
		ProgramEnded,
		StackOverflow,
		MaxStepsReached,
	}
	
	public boolean isFunctionEnded(){
		return program.getFunctionLength(ip.function) <= ip.command;
	}
	
	private boolean isProgramEnded() {
		return isFunctionEnded() && stack.isEmpty();
	}

	public interface CallListener{
		void onFunctionEnter(int func);
		void onFunctionExit();
	}

	CallListener callListener;
	public void setCallListener(CallListener callListener){
		this.callListener = callListener;
	}

	private void onFunctionEnter(int func){
		if (callListener != null)
			callListener.onFunctionEnter(func);
	}

	private void onFunctionExit(){
		if (callListener != null)
			callListener.onFunctionExit();
	}
	
	public GameEnd step(){
		if (this.getStepsMade() >= MAX_STEPS)
			return GameEnd.MaxStepsReached;

		int iterations = 100000000;
		while(iterations-- > 0){
			if (stars.size() == 0){
				Exception error = new IllegalStateException("Robot has already won!\n" +
						"Puzzle: " + puzzleID + "\n" +
						"Program: " + program.getProgram());
				// TODo: report exception
				return GameEnd.Win;
			}
			if (isBlack(x, y) || isProgramEnded()){
				Exception error = new IllegalStateException("Robot has already lost!\n" +
					"Puzzle: " + puzzleID + "\n" +
					"Program: " + program.getProgram());
				// TODO: report exception
				return isBlack(x, y)? GameEnd.Fall: GameEnd.ProgramEnded;
			}
			
			while (isFunctionEnded()) {
				ip = stack.pop();
				this.onFunctionExit();
				if (isProgramEnded()) return GameEnd.ProgramEnded;
			}
			
			if (program.actions[ip.function][ip.command] == '\0' || program.actions[ip.function][ip.command] == '_') {
				ip.Step();
				this.totalSteps++;
				if (isProgramEnded()) return GameEnd.ProgramEnded;
				continue;
			}
			
			if (program.colors[ip.function][ip.command] != Character.toLowerCase(colors[x][y]) &&
					program.colors[ip.function][ip.command] != '_'){
				this.totalSteps++;
				ip.Step();
				if (isProgramEnded()) return GameEnd.ProgramEnded;
				continue;
			}
			
			break;
		}
		if (iterations < 10){
			Exception error = new IllegalStateException("Stack overflow while executing step.\n"+
					"Puzzle: " + puzzleID + "\n" +
					"Program: " + program.getProgram());
			// TODO: report exception
			return GameEnd.StackOverflow;
		}

		this.totalSteps++;
		this.actionCount++;

		char action = program.actions[ip.function][ip.command]; 
		switch(action){
		case Action.BLUE: case Action.RED: case Action.GREEN:
			colors[x][y] = Character.toUpperCase(action);
			break;
		
		case Action.TURN_LEFT:
			direction = Direction.TurnLeft(direction);
			break;
			
		case Action.TURN_RIGHT:
			direction = Direction.TurnRight(direction);
			break;
		
		case Action.FORWARD:
			switch(direction){
			case Direction.RIGHT:
				x++;
				break;
			case Direction.LEFT:
				x--;
				break;
			case Direction.TOP:
				y--;
				break;
			case Direction.DOWN:
				y++;
				break;
			}
			
			Point pos = new Point(x, y);
			stars.remove(pos);
			if (stars.size() == 0) return GameEnd.Win;
			
			if (isBlack(x, y)) return GameEnd.Fall;
			break;
			
		default:
			if (action < '1' || action > '5') throw new IllegalStateException("Unknown action: "+ action);
			// function call is not considered an action
			this.actionCount--;
			int func = action - '1';
			ip.Step();
			if (program.actions[func].length > 0){
				stack.push(ip);
				this.onFunctionEnter(func);
				if (stack.size() > MAX_STACK) return GameEnd.StackOverflow;
				ip = new IP(func, 0);
			}
			return GameEnd.No;
		}

		ip.Step();
		while (isFunctionEnded()) {
			if (stack.isEmpty()) return GameEnd.ProgramEnded;
			ip = stack.pop();
		}
		return GameEnd.No;
	}
	
	public static final int MAX_STACK = 4096;
	public static final int MAX_STEPS = 1000;
	
	public boolean isWin(){
		return stars.size() == 0;
	}
}
