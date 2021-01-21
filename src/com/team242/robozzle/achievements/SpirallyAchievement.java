package com.team242.robozzle.achievements;

import com.team242.robozzle.R;
import com.team242.robozzle.model.Puzzle.Point;
import com.team242.robozzle.model.Puzzle;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by lost on 2/6/14.
 */
public class SpirallyAchievement extends PuzzleCountAchievement {
    private SpirallyAchievement(){
        super(R.string.spirallyTitle, R.string.spirallyDescription, R.drawable.spirally, ALL_MATCHING);
    }

    public boolean puzzleMatches(Puzzle puzzle){
        if (!super.puzzleMatches(puzzle))
            return false;

        return isSpiral(puzzle);
    }

    public static final Achievement Instance = new SpirallyAchievement();

    static final int UNVISITED = 0, PASSABLE = Integer.MAX_VALUE;

    static boolean isSpiral(Puzzle puzzle) {
        final int spiralDistance = (puzzle.getWidth() + puzzle.getHeight())*2;
        int[][] distanceFromEdge = new int[puzzle.getWidth()][puzzle.getHeight()];

        Queue<Point> toProcess = setEdgeCellDistances(puzzle, distanceFromEdge);
        while (!toProcess.isEmpty()){
            Point cell = toProcess.poll();
            int distance = distanceFromEdge[cell.x][cell.y];
            if (distance == PASSABLE)
                continue;

            if (distance >= spiralDistance)
                return true;

            for(Point neightboor: cell.validNeighbors(puzzle)){
                if (distanceFromEdge[neightboor.x][neightboor.y] != UNVISITED)
                    continue;

                setDistance(distanceFromEdge, puzzle, neightboor, distance + 1);
                toProcess.add(neightboor);
            }
        }

        return false;
    }

    private static Queue<Point> setEdgeCellDistances(Puzzle puzzle, int[][] distanceFromEdge) {
        Queue<Point> edgeCells = new LinkedList<Point>();
        // on edges, distance equals to 1, if cell is passable
        for (int x = 0; x < puzzle.getWidth(); x++) {
            edgeCells.add(new Point(x, 0));
            edgeCells.add(new Point(x, puzzle.getHeight() - 1));
        }
        for (int y = 1; y < puzzle.getHeight() - 1; y++) {
            edgeCells.add(new Point(0, y));
            edgeCells.add(new Point(puzzle.getWidth() - 1, y));
        }
        for(Point edgeCell: edgeCells)
            setDistance(distanceFromEdge, puzzle, edgeCell, 1);

        return edgeCells;
    }

    static void setDistance(int[][] distanceFromEdge, Puzzle puzzle, Point coord, int distance) {
        distanceFromEdge[coord.x][coord.y] = puzzle.isBlack(coord.x, coord.y)? distance : PASSABLE;
    }
}
