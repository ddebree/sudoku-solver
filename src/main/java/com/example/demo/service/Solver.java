package com.example.demo.service;

import com.example.demo.model.Cell;
import com.example.demo.model.SudokuBoard;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class Solver {

    public Set<SudokuBoard> findSolutions(final SudokuBoard board) {
        if (! board.isValid()) {
            return Collections.emptySet();
        }
        if (board.isSolved()) {
            return Collections.singleton(board);
        }
        //Look for naked candidates (a cell with only one possible value):
        for (Cell cell : board.getUnsolvedCells()) {
            SortedSet<Integer> possibleValues = board.getPossibleValues(cell);
            if (possibleValues.size() == 1) {
                return findSolutions(board.withValue(cell, possibleValues.first()));
            }
        }

        //Brute force it...
        final Optional<Cell> firstUnset = board.getUnsolvedCells().stream().findAny();
        if (firstUnset.isPresent()) {
            final Cell cell = firstUnset.get();
            return board.getPossibleValues(cell).parallelStream()
                    .map(value -> findSolutions(board.withValue(cell, value)))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
        } else {
            throw new RuntimeException("Something weird here");
        }
    }

}
