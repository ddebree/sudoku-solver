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

        EnumSet<Value> fewestPossibles = EnumSet.allOf(Value.class);
        Position fewestPossiblePosition = null;

        for (final Position position : board.getUnsolvedPositions()) {
            final EnumSet<Value> possibleValues = board.getPossibleValues(position);
            if (possibleValues.size() == 1) {
                return findSolutions(board.withValue(position, possibleValues.iterator().next()));
            } else if (possibleValues.size() < fewestPossibles.size()) {
                fewestPossibles = possibleValues;
                fewestPossiblePosition = position;
            }
        }

        //Brute force it...
        final Position position = fewestPossiblePosition;
        return fewestPossibles
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
    }

}
