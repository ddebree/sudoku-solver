package com.example.demo.model;

import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class SudokuBoard {

    private static final AtomicReference<SudokuBoard> EMPTY_BOARD = new AtomicReference<>();
    private static final SortedSet<Integer> POSSIBLE_VALUES = new TreeSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));

    @Getter
    private final Cell[][] cells;

    private final Map<Cell, Set<Cell>> neighbours = new ConcurrentHashMap<>();
    private final Map<Cell, SortedSet<Integer>> neighbourValues = new ConcurrentHashMap<>();
    private final Map<Cell, SortedSet<Integer>> possibleValues = new ConcurrentHashMap<>();

    private SudokuBoard(final Cell[][] cells) {
        this.cells = cells;
    }

    public Cell getCell(int row, int col) {
        return this.cells[row][col];
    }

    public Set<Cell> getCellSet() {
        final var cells = new HashSet<Cell>();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                cells.add(this.cells[row][col]);
            }
        }
        return cells;
    }

    public Set<Cell> getNeighbours(final int row, final int col) {
        return getNeighbours(getCell(row, col));
    }

    public Set<Cell> getNeighbours(final Cell cell) {
        return this.neighbours.computeIfAbsent(cell, c -> {
            final Set<Cell> neighbours = new HashSet<>();
            final int blockRow = (c.getRow() / 3) * 3;
            final int blockCol = (c.getCol() / 3) * 3;
            for (int i = 0; i < 9; i++) {
                //Rows:
                neighbours.add(this.cells[c.getRow()][i]);
                //Cols:
                neighbours.add(this.cells[i][c.getCol()]);
                //
                neighbours.add(this.cells[blockRow + (i / 3)][blockCol + (i % 3)]);
            }
            neighbours.remove(c);
            return neighbours;
        });
    }

    public SortedSet<Integer> getNeighbourValues(final int row, final int col) {
        return getNeighbourValues(getCell(row, col));
    }

    public SortedSet<Integer> getNeighbourValues(final Cell cell) {
        return this.neighbourValues.computeIfAbsent(cell,
                c -> getNeighbours(c).stream()
                        .filter(a -> a.getValue().isPresent())
                        .map(a -> a.getValue().getAsInt())
                        .collect(Collectors.toCollection(TreeSet::new)));
    }

    public SortedSet<Integer> getPossibleValues(final int row, final int col) {
        return getPossibleValues(getCell(row, col));
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

    public boolean isValid() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                final var cell = cells[row][col];
                if (cell.getValue().isPresent()) {
                    //This is set, so it shouldn't conflict with other cells:
                    if (getNeighbourValues(cell).contains(cell.getValue().getAsInt())) {
                        //One of the neighbours has the same value, so reject:
                        return false;
                    }
                } else {
                    if (getPossibleValues(cell).isEmpty()) {
                        //no possible value for this cell:
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public Optional<Cell> getFirstUnset() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                var cell = this.cells[row][col];
                if (cell.getValue().isEmpty()) {
                    return Optional.of(cell);
                }
            }
        }
        return Optional.empty();
    }

    public int countSolutions() {
        if (! isValid()) {
            return 0;
        }
        final Optional<Cell> firstUnset = getFirstUnset();
        if (firstUnset.isPresent()) {
            final Cell cell = firstUnset.get();
            return getPossibleValues(cell).parallelStream()
                    .mapToInt(value -> withValue(cell, value).countSolutions())
                    .sum();
        } else {
            //We have a valid solution!
            return 1;
        }
    }

    public Set<SudokuBoard> findSolutions() {
        if (! isValid()) {
            return Collections.emptySet();
        }
        final Optional<Cell> firstUnset = getFirstUnset();
        if (firstUnset.isPresent()) {
            final Cell cell = firstUnset.get();
            return getPossibleValues(cell).parallelStream()
                    .map(value -> withValue(cell, value).findSolutions())
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
        } else {
            //We have a valid solution!
            return Collections.singleton(this);
        }
    }

    public static SudokuBoard empty() {
        if (EMPTY_BOARD.get() == null) {
            final var cells = new Cell[9][];
            for (int row = 0; row < 9; row++) {
                cells[row] = new Cell[9];
                for (int col = 0; col < 9; col++) {
                    cells[row][col] = new Cell(row, col);
                }
            }
            EMPTY_BOARD.compareAndSet(null, new SudokuBoard(cells));
        }
        return EMPTY_BOARD.get();
    }

    public SudokuBoard withValue(final Cell cell, final int setValue) {
        return withValue(cell.getRow(), cell.getCol(), setValue);
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
                    cells[row][col] = new Cell(row, col, setValue);
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
