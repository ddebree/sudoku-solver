package com.example.demo.service;

import com.example.demo.exception.ValueAlreadySetException;
import com.example.demo.model.Position;
import com.example.demo.model.SudokuBoard;
import com.example.demo.model.Value;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class Solver {

    @SneakyThrows
    public Set<SudokuBoard> findSolutions(final SudokuBoard board) {
        if (! board.isValid()) {
            return Collections.emptySet();
        }
        if (board.isSolved()) {
            return Collections.singleton(board);
        }
        //Look for naked candidates (a cell with only one possible value):
        for (final Position position : board.getUnsolvedPositions()) {
            final EnumSet<Value> possibleValues = board.getPossibleValues(position);
            if (possibleValues.size() == 1) {
                return findSolutions(board.withValue(position, possibleValues.iterator().next()));
            }
        }

        //Brute force it...
        final Optional<Position> firstUnset = board.getUnsolvedPositions().stream().findAny();
        if (firstUnset.isPresent()) {
            final Position position = firstUnset.get();
            return board.getPossibleValues(position)
                    .parallelStream()
                    .map(value -> {
                        try {
                            return findSolutions(board.withValue(position, value));
                        } catch (ValueAlreadySetException e) {
                            return Collections.<SudokuBoard>emptySet();
                        }
                    })
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
        } else {
            throw new RuntimeException("Something weird here");
        }
    }

}
