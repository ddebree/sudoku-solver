package com.example.demo.model;

import com.example.demo.exception.ConflictingNeighbourValueException;
import com.example.demo.exception.ValueAlreadySetException;

import java.util.HashSet;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

public class SudokuBoard {

    private final Cell[][] cells;

    private SudokuBoard() {
        this.cells = new Cell[9][];
        for (int row = 0; row < 9; row++) {
            cells[row] = new Cell[9];
            for (int col = 0; col < 9; col++) {
                cells[row][col] = new Cell();
            }
        }

        //Add neighbours using rows:
        for (int row = 0; row < 9; row++) {
            final Set<Cell> allInRow = new HashSet<>();
            for (int col = 0; col < 9; col++) {
                allInRow.add(this.cells[row][col]);
            }
            allInRow.forEach(c -> c.addNeighbours(allInRow));
        }

        //Add neighbours using cols:
        for (int col = 0; col < 9; col++) {
            final Set<Cell> allInCol = new HashSet<>();
            for (int row = 0; row < 9; row++) {
                allInCol.add(this.cells[row][col]);
            }
            allInCol.forEach(c -> c.addNeighbours(allInCol));
        }

        //Add neighbours using blocks:
        for (int blockCol = 0; blockCol < 3; blockCol++) {
            for (int blockRow = 0; blockRow < 3; blockRow++) {
                final Set<Cell> allInBlock = new HashSet<>();
                for (int colOffset = 0; colOffset < 3; colOffset++) {
                    for (int rowOffset = 0; rowOffset < 3; rowOffset++) {
                        allInBlock.add(this.cells[(blockRow * 3) + rowOffset][(blockCol * 3) + colOffset]);
                    }
                }
                allInBlock.forEach(c -> c.addNeighbours(allInBlock));
            }
        }
    }

    public static SudokuBoard empty() {
        return new SudokuBoard();
    }

    public SudokuBoard withValue(final int setRow, final int setCol, final int setValue)
            throws ValueAlreadySetException, ConflictingNeighbourValueException {
        final var result = new SudokuBoard();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                result.cells[row][col].setValue(this.cells[row][col].getValue());
            }
        }
        var cell = result.cells[setRow][setCol];
        if (cell.getValue().isPresent() && cell.getValue().getAsInt() != setValue) {
            throw new ValueAlreadySetException();
        } else if (cell.getNeighbourValues().contains(setValue)) {
            throw new ConflictingNeighbourValueException();
        }
        result.cells[setRow][setCol].setValue(OptionalInt.of(setValue));
        return result;
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
