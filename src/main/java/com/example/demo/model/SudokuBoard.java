package com.example.demo.model;

import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class SudokuBoard {

    private static final AtomicReference<SudokuBoard> EMPTY_BOARD = new AtomicReference<>();
    private static final SortedSet<Integer> POSSIBLE_VALUES = new TreeSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
    private static final Map<Position, Set<Position>> NEIGHBOURS = new ConcurrentHashMap<>();
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

    @Getter
    private final Cell[][] cells;

    private transient Boolean valid = null;
    private transient Boolean solved = null;
    private transient Set<Cell> unsolvedCells = null;
    private final Map<Cell, SortedSet<Integer>> neighbourValues = new ConcurrentHashMap<>();
    private final Map<Cell, SortedSet<Integer>> possibleValues = new ConcurrentHashMap<>();

    private SudokuBoard(final Cell[][] cells) {
        this.cells = cells;
    }

    public Set<Cell> getUnsolvedCells() {
        if (unsolvedCells == null) {
            final var cells = new HashSet<Cell>();
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    final var cell = this.cells[row][col];
                    if (cell.getValue().isEmpty()) {
                        cells.add(cell);
                    }
                }
            }
            unsolvedCells = cells;
        }
        return unsolvedCells;
    }

    public Set<Position> getNeighbours(final Position position) {
        return NEIGHBOURS.computeIfAbsent(position, p -> {
            final Set<Position> neighbours = new HashSet<>();
            final int blockRow = (p.getRow() / 3) * 3;
            final int blockCol = (p.getCol() / 3) * 3;
            for (int i = 0; i < 9; i++) {
                //Rows:
                neighbours.add(new Position(p.getRow(), i));
                //Cols:
                neighbours.add(new Position(i, p.getCol()));
                //
                neighbours.add(new Position(blockRow + (i / 3), blockCol + (i % 3)));
            }
            neighbours.remove(p);
            return neighbours;
        });
    }

    public SortedSet<Integer> getNeighbourValues(final Cell cell) {
        return this.neighbourValues.computeIfAbsent(cell,
                c -> getNeighbours(c.getPosition()).stream()
                        .map(this::getCell)
                        .filter(a -> a.getValue().isPresent())
                        .map(a -> a.getValue().getAsInt())
                        .collect(Collectors.toCollection(TreeSet::new)));
    }

    private Cell getCell(Position position) {
        return this.cells[position.getRow()][position.getCol()];
    }

    public SortedSet<Integer> getPossibleValues(final Cell cell) {
        return this.possibleValues.computeIfAbsent(cell,
                c -> {
                    if (cell.getValue().isPresent()) {
                        return new TreeSet<>(List.of(cell.getValue().getAsInt()));
                    } else {
                        final TreeSet<Integer> potentialValues = new TreeSet<>(POSSIBLE_VALUES);
                        potentialValues.removeAll(getNeighbourValues(c));
                        return potentialValues;
                    }
                });
    }

    public boolean isSolved() {
        if (solved != null) {
            return solved;
        }
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                final var cell = cells[row][col];
                if (cell.getValue().isEmpty()) {
                    solved = false;
                    return false;
                }
            }
        }
        solved = true;
        return true;
    }

    public boolean isValid() {
        if (valid != null) {
            return valid;
        }
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                final var cell = cells[row][col];
                if (cell.getValue().isPresent()) {
                    //This is set, so it shouldn't conflict with other cells:
                    if (getNeighbourValues(cell).contains(cell.getValue().getAsInt())) {
                        //One of the neighbours has the same value, so reject:
                        return valid = false;
                    }
                } else {
                    if (getPossibleValues(cell).isEmpty()) {
                        //no possible value for this cell:
                        return valid = false;
                    }
                }
            }
        }
        return valid = true;
    }

    public static SudokuBoard empty() {
        if (EMPTY_BOARD.get() == null) {
            final var cells = new Cell[9][];
            for (int row = 0; row < 9; row++) {
                cells[row] = new Cell[9];
                for (int col = 0; col < 9; col++) {
                    cells[row][col] = new Cell(new Position(row, col));
                }
            }
            EMPTY_BOARD.compareAndSet(null, new SudokuBoard(cells));
        }
        return EMPTY_BOARD.get();
    }

    public SudokuBoard withValue(final Cell cell, final int setValue) {
        return withValue(cell.getPosition().getRow(), cell.getPosition().getCol(), setValue);
    }

    public SudokuBoard withValue(final int setRow, final int setCol, final int setValue) {
        final var setCell = this.cells[setRow][setCol];
        if (setCell.getValue().isPresent() && setCell.getValue().getAsInt() == setValue) {
            return this;
        }
        final Cell[][] cells = new Cell[9][];
        for (int row = 0; row < 9; row++) {
            cells[row] = new Cell[9];
            for (int col = 0; col < 9; col++) {
                if (setRow == row && setCol == col) {
                    cells[row][col] = new Cell(new Position(row, col), setValue);
                } else {
                    cells[row][col] = this.cells[row][col];
                }
            }
        }
        return new SudokuBoard(cells);
    }

    public String toString() {
        final var stringBuilder = new StringBuilder();
        for (int row = 0; row < 9; row++) {
            stringBuilder.append("$");
            for (int col = 0; col < 9; col++) {
                final var cell = this.cells[row][col];
                stringBuilder.append(" ");
                if (cell.getValue().isPresent()) {
                    stringBuilder.append(cell.getValue().getAsInt());
                } else {
                    stringBuilder.append(".");
                }
                if (col % 3 == 2) {
                    stringBuilder.append(" $");
                } else {
                    stringBuilder.append(" |");
                }
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

}
