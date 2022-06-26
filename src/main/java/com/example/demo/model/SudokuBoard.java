package com.example.demo.model;

import com.example.demo.exception.ValueAlreadySetException;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class SudokuBoard {

    private static final SudokuBoard EMPTY_BOARD = new EmptySudokuBoard(Collections.emptyMap());

    @Getter
    protected final Map<Position, Integer> values;

    private SudokuBoard(final Map<Position, Integer> values) {
        this.values = values;
    }

    public abstract Set<Position> getUnsolvedPositions();

    public abstract SortedSet<Integer> getNeighbourValues(final Position position);

    public abstract SortedSet<Integer> getPossibleValues(final Position position);

    public abstract boolean isSolved();

    public abstract boolean isValid();

    public static SudokuBoard empty() {
        return EMPTY_BOARD;
    }

    public SudokuBoard withValue(final int setRow, final int setCol, final int setValue) throws ValueAlreadySetException {
        return withValue(Position.at(setRow, setCol), setValue);
    }

    public SudokuBoard withValue(final Position position, final int setValue) throws ValueAlreadySetException {
        if (this.values.containsKey(position)) {
            if (this.values.get(position) == setValue) {
                return this;
            } else {
                throw new ValueAlreadySetException();
            }
        }
        if (this.values.containsKey(position) && this.values.get(position) == setValue) {
            return this;
        }
        final Map<Position, Integer> values = new HashMap<>(this.values);
        values.put(position, setValue);
        return new ChildSudokuBoard(this, values);
    }

    public String toString() {
        final var stringBuilder = new StringBuilder();
        for (int row = 0; row < 9; row++) {
            stringBuilder.append("$");
            for (int col = 0; col < 9; col++) {
                final var position = Position.at(row, col);
                stringBuilder.append(" ");
                if (this.values.containsKey(position)) {
                    stringBuilder.append(this.values.get(position));
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

    private static class EmptySudokuBoard extends SudokuBoard {

        private EmptySudokuBoard(Map<Position, Integer> values) {
            super(values);
        }

        public Set<Position> getUnsolvedPositions() {
            return Position.ALL_POSITIONS;
        }

        public boolean isValid() {
            return true;
        }

        public boolean isSolved() {
            return false;
        }

        public SortedSet<Integer> getNeighbourValues(final Position position) {
            return Collections.emptySortedSet();
        }

        public SortedSet<Integer> getPossibleValues(final Position position) {
            return Value.POSSIBLE_VALUES;
        }

    }

    private static class ChildSudokuBoard extends SudokuBoard {

        private final SudokuBoard parent;

        private transient Boolean valid = null;
        private transient Set<Position> unsolvedPositions = null;
        private final Map<Position, SortedSet<Integer>> neighbourValues = new ConcurrentHashMap<>();
        private final Map<Position, SortedSet<Integer>> possibleValues = new ConcurrentHashMap<>();

        private ChildSudokuBoard(SudokuBoard parent, Map<Position, Integer> values) {
            super(values);
            this.parent = parent;
        }

        public Set<Position> getUnsolvedPositions() {
            if (unsolvedPositions == null) {
                unsolvedPositions = computeUnsolvedPositions();
            }
            return unsolvedPositions;
        }

        private Set<Position> computeUnsolvedPositions() {
            return parent.getUnsolvedPositions().stream()
                    .filter(p -> ! this.values.containsKey(p))
                    .collect(Collectors.toSet());
        }

        public boolean isValid() {
            if (valid == null) {
                valid = computeValid();
            }
            return valid;
        }

        private boolean computeValid() {
            for (final Position position : Position.ALL_POSITIONS) {
                if (this.values.containsKey(position)) {
                    //This is set, so it shouldn't conflict with other cells:
                    if (getNeighbourValues(position).contains(this.values.get(position))) {
                        //One of the neighbours has the same value, so reject:
                        return false;
                    }
                } else {
                    if (getPossibleValues(position).isEmpty()) {
                        //no possible value for this cell:
                        return false;
                    }
                }
            }
            return true;
        }

        public boolean isSolved() {
            return this.values.size() == Position.ALL_POSITIONS.size();
        }

        public SortedSet<Integer> getNeighbourValues(final Position position) {
            return this.neighbourValues.computeIfAbsent(position, this::computeNeighbourValues);
        }

        private SortedSet<Integer> computeNeighbourValues(final Position position) {
            return Position.getNeighbours(position).stream()
                    .filter(this.values::containsKey)
                    .map(this.values::get)
                    .collect(Collectors.toCollection(TreeSet::new));
        }

        public SortedSet<Integer> getPossibleValues(final Position position) {
            return this.possibleValues.computeIfAbsent(position, this::computePossibleValues);
        }

        private SortedSet<Integer> computePossibleValues(final Position position) {
            if (this.values.containsKey(position)) {
                return new TreeSet<>(List.of(this.values.get(position)));
            } else {
                final TreeSet<Integer> potentialValues = new TreeSet<>(parent.getPossibleValues(position));
                potentialValues.removeAll(getNeighbourValues(position));
                return potentialValues;
            }
        }

    }

}
