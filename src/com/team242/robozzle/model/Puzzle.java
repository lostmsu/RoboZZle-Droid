/**
 * 
 */
package com.team242.robozzle.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.team242.util.MathEx;

import java.util.*;

/**
 * @author lost
 *
 */
@DatabaseTable(tableName = "PUZZLES")
public class Puzzle {
    public static final char EMPTY_CELL = '#';
    public static final char BLACK = '\0';

	public Puzzle() {
	}
	
	@DatabaseField
	public String about;
	@DatabaseField(canBeNull = false)
	public int allowedCommands;

	public boolean hasDescription(){
		return (about != null) && (about.length() > 0) && !"anyType{}".equals(about);
	}
	
	public boolean allowedPaint(char color){
		switch(color){
		case 'R':
			return (allowedCommands & 1) > 0;
		case 'G':
			return (allowedCommands & 2) > 0;
		default:
			return (allowedCommands & 4) > 0;
		}
	}
	public int allowedPaints(){
		int x = 0;
		if (allowedPaint('R')) x++;
		if (allowedPaint('G')) x++;
		if (allowedPaint('B')) x++;
		return x;
	}
	
	public static boolean isBlack(char color){
		switch(color){
		case 'R':
		case 'G':
		case 'B':
			return false;
		default:
			return true;
		}
	}
	
	public boolean isBlack(int x, int y){
		return isBlack(getColor(x, y));
	}

	public boolean isTutorial(){
		return "l0st".equalsIgnoreCase(submittedBy) && difficulty <= 10;
	}
	
	public boolean isPopular(){
		return liked >= 128 || isTutorial();
	}
	
	public static final int WIDTH = 16;
	public static final int HEIGHT = 12;
	
	@DatabaseField(canBeNull = false, useGetSet = true)
	String colors;
	public String getColors(){
		return colors;
	}
	public String getColors(int y){
		return colors.substring(y * WIDTH, (y + 1) * WIDTH);
	}
	public void setColors(String[] colors){
		StringBuilder result = new StringBuilder(WIDTH * HEIGHT);
		for(String colorLine: colors)
			result.append(colorLine);
		setColors(result.toString());
	}
	public void setColors(String colors){
		if (this.colors == null){
			this.colors = colors;
		} else
			throw new IllegalStateException();
	}

	public boolean hasColor(char color){
        if (allowedPaint(color))
            return true;

        int index = 0;
        while (index < colors.length()){
            index = colors.indexOf(color, index);

            if (index < 0)
                return false;

            if (items.charAt(index) != EMPTY_CELL)
                return true;

            index++;
        }

        return false;
	}

	public int colorCount(){
		int x = 0;
		if (hasColor('R')) x++;
		if (hasColor('G')) x++;
		if (hasColor('B')) x++;
		return x;
	}

	public char getColor(int x, int y){
		return getItem(x, y) == EMPTY_CELL
			? BLACK
			: colors.charAt(y * WIDTH + x);
	}

	@DatabaseField(canBeNull = false)
	public int commentCount;
	@DatabaseField(canBeNull = false)
	public int difficulty;
	
	@DatabaseField(canBeNull = false)
	public int disliked;
	@DatabaseField(canBeNull = false)
	public boolean featured;
	@DatabaseField(id = true, canBeNull = false)
	public int id;
	
	Set<Point> stars;
	@DatabaseField(useGetSet = true, canBeNull = false)
	String items;
	public String getItems(int y){
		return items.substring(y * WIDTH, (y + 1) * WIDTH);
	}
	public String getItems(){
		return items;
	}
	public void setItems(String[] items){
		StringBuilder result = new StringBuilder(WIDTH * HEIGHT);
		for(String itemLine: items)
			result.append(itemLine);
		setItems(result.toString());
	}
	public void setItems(String items){
		if (this.items == null) {
			this.items = items;
		}
		else
			throw new IllegalStateException();
	}
	public char getItem(int x, int y){
		return items.charAt(y * WIDTH + x);
	}

	public int getWidth(){
		if (items.length() != WIDTH * HEIGHT)
			assert(false);
		return WIDTH;
	}
	public int getHeight(){
		if (items.length() != WIDTH * HEIGHT)
			assert(false);
		return HEIGHT;
	}
	
	private void fillStars() {
		HashSet<Point> stars = new HashSet<>();
		for(int y = HEIGHT - 1; y >= 0; y--){
			for(int x = WIDTH - 1; x >= 0; x--){
				if (getItem(x, y) == '*')
					stars.add(new Point(x, y));
			}
		}
		this.stars = Collections.unmodifiableSet(stars);
	}
	public Collection<Point> getStars(){
		if (this.stars == null)
			fillStars();
		return this.stars;
	}

	@DatabaseField(canBeNull = false)
	public int liked;
	
	@DatabaseField(canBeNull = false)
	public int robotCol;
	@DatabaseField(canBeNull = false)
	public int robotDir;
	@DatabaseField(canBeNull = false)
	public int robotRow;
	
	@DatabaseField(canBeNull = false)
	public int solutions;
	@DatabaseField(canBeNull = false)
	String subLengths;
	public int getFunctionLength(int function){
		char result = subLengths.charAt(function);
		if (result == '\0') return 0;
		return Character.digit(result, 16) + 1;
	}
	public void setFunctionLengths(int[] lengths){
		StringBuilder res = new StringBuilder(lengths.length);
		for(int l: lengths)
			if (l == 0)
				res.append('\0');
			else
				res.append(Character.forDigit(l - 1, 16));
		subLengths = res.toString();
	}
	public int getFunctionCount(){
		int count = subLengths.length();
		while (count > 0 && subLengths.charAt(count-1) == '\0') count--;
		return count;
	}
	
	@DatabaseField
	public String submittedBy;
	public Date submittedDate;
	@DatabaseField(canBeNull = false)
	public String title;
	
	@DatabaseField(useGetSet = true)
	public String solution;

	@DatabaseField(canBeNull = false)
	public boolean hasNewSolution;
	
	Program solutionProgram;
	public Program getSolutionProgram(){
		if (solutionProgram == null)
			solutionProgram = new Program(this, getSolution());
		
		return solutionProgram;
	}
	
	public String getSolution(){
		String program = solutionProgram == null
			? solution
			: solutionProgram.getProgram();
		
		if (program == null) return null;
		
		StringBuilder result = new StringBuilder();
		for(int i = 0; i < program.length(); i++){
			char c = program.charAt(i); 
			if (c != '\0') result.append(c);
			else result.append('_');
		}
		
		program = result.toString().replace("__", "");
		
		return program;
	}
	public void setSolution(String solution){
		if (solutionProgram == null) this.solution = solution;
		else solutionProgram.setProgram(solution.replace("\0", ""));

		if (solution != null && solution.length() != 0) hasNewSolution = true;
	}
	
	@DatabaseField(defaultValue = "", useGetSet = true)
	public String program;

	@DatabaseField(defaultValue = "0", canBeNull = false)
	int userLike;
	public int getUserLike(){return userLike;}
	public void setUserLike(int value){
		if (value > 1 || value < -1) throw new IllegalArgumentException();
		userLike = value;
	}

	@DatabaseField(defaultValue = "0", canBeNull = false)
	int scary;
	public int getScary(){return scary;}
	public void setScary(int value){
		if (value < 0) throw new IllegalArgumentException();
		scary = value;
	}
	public void imScared(){scary++;}

	@DatabaseField(defaultValue = "0", canBeNull = false)
	int userDifficulty;
	public int getUserDifficulty(){return userDifficulty;}
	public void setUserDifficulty(int value){
		if (value < 0 || value > 5) throw new IllegalArgumentException();
		userDifficulty = value;
	}
	
	Program currentProgram;
	public Program getCurrentProgram(){
		if (currentProgram == null)
			currentProgram = new Program(this, program);
		
		return currentProgram;
	}
	
	public String getProgram(){
		return currentProgram == null
			? program
			: currentProgram.getProgram();
	}
	
	public void setProgram(String value){
		if (currentProgram == null)
			program = value;
		else
			currentProgram.setProgram(value);
	}
	
	public static class Point{
		public Point(int x, int y){
			this.x = x;
			this.y = y;
		}
		
		public final int x;
		public final int y;
		
		public Point next(int direction){
			direction = MathEx.rem(direction, 4);
			switch(direction){
			case 0:
				return new Point(x + 1, y);
			case 1:
				return new Point(x, y - 1);
			case 2:
				return new Point(x - 1, y);
			default:
				return new Point(x, y + 1);
			}
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Point))
				return false;
			
			Point other = (Point)o;
			return this.x == other.x && this.y == other.y;
		}
		
		@Override
		public int hashCode() {
			return ((Integer)x).hashCode() ^ ((Integer)(y + 256)).hashCode();
		}
		
		@Override
		public String toString() {
			return "(" + x + ", " + y + ")";
		}

        public List<Point> validNeighbors(Puzzle puzzle){
            List<Point> neighbors = new ArrayList<>(4);
            if (x > 0)
                neighbors.add(new Point(x - 1, y));
            if (x + 1 < puzzle.getWidth())
                neighbors.add(new Point(x + 1, y));
            if (y > 0)
                neighbors.add(new Point(x, y - 1));
            if (y + 1 < puzzle.getHeight())
                neighbors.add(new Point(x, y + 1));
            return neighbors;
        }
	}

	public static boolean valid(int x, int y){
		return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
	}
}
