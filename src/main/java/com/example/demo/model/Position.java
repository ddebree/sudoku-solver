package com.example.demo.model;

import lombok.Data;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Position {

    public static final Set<Position> ALL_POSITIONS = new HashSet<>();
    public static final Position[][] POSITIONS = new Position[9][];
    static {
        for (int row = 0; row < 9; row++) {
            POSITIONS[row] = new Position[9];
            for (int col = 0; col < 9; col++) {
                var position = new Position(row, col);
                ALL_POSITIONS.add(position);
                POSITIONS[row][col] = position;
            }
        }
    }

    private static final Map<Position, Set<Position>> NEIGHBOURS = new ConcurrentHashMap<>();

    private final int row;
    private final int col;

    private Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public static Position at(int row, int col) {
        return POSITIONS[row][col];
    }

    public static Set<Position> getNeighbours(final Position position) {
        return NEIGHBOURS.computeIfAbsent(position, p -> {
            final Set<Position> neighbours = new HashSet<>();
            final int blockRow = (p.getRow() / 3) * 3;
            final int blockCol = (p.getCol() / 3) * 3;
            for (int i = 0; i < 9; i++) {
                //Rows:
                neighbours.add(at(p.getRow(), i));
                //Cols:
                neighbours.add(at(i, p.getCol()));
                //
                neighbours.add(at(blockRow + (i / 3), blockCol + (i % 3)));
            }
            neighbours.remove(p);
            return neighbours;
        });
    }
}
