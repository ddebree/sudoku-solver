package com.example.demo.model;

import com.example.demo.exception.ValueAlreadySetException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class SudokuBoard {

    protected abstract Map<Position, Integer> getValues();

    public abstract boolean hasValue(Position position);
    public abstract OptionalInt getValue(Position position);
    public Integer getValueUnsafe(Position position) {
        return getValue(position).orElseThrow();
    }

    public abstract Set<Position> getUnsolvedPositions();

    public abstract SortedSet<Integer> getPossibleValues(final Position position);

    public abstract boolean isSolved();

    public abstract boolean isValid();

    public static SudokuBoard empty() {
        return EmptySudokuBoard.INSTANCE;
    }

    public SudokuBoard withValue(final Position position, final int value) throws ValueAlreadySetException {
        if (this.hasValue(position)) {
            if (this.getValueUnsafe(position) == value) {
                return this;
            } else {
                throw new ValueAlreadySetException();
            }
        }
        if (this.hasValue(position) && this.getValueUnsafe(position) == value) {
            return this;
        }
        return new ChildSudokuBoard(this, position, value);
    }

    private static class EmptySudokuBoard extends SudokuBoard {

        private static final SudokuBoard INSTANCE = new EmptySudokuBoard();

        @Override
        public Map<Position, Integer> getValues() {
            return Collections.emptyMap();
        }

        @Override
        public boolean hasValue(Position position) {
            return false;
        }

        @Override
        public OptionalInt getValue(Position position) {
            return OptionalInt.empty();
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

        public SortedSet<Integer> getPossibleValues(final Position position) {
            return Value.POSSIBLE_VALUES;
        }

    }

    private static class ChildSudokuBoard extends SudokuBoard {

        private final SudokuBoard parent;

        private final Map<Position, Integer> values;
        private transient Boolean valid = null;
        private transient Set<Position> unsolvedPositions = null;
        private final Map<Position, SortedSet<Integer>> neighbourValues = new ConcurrentHashMap<>();
        private final Map<Position, SortedSet<Integer>> possibleValues = new ConcurrentHashMap<>();

        private ChildSudokuBoard(SudokuBoard parent, Position position, Integer value) {
            this.parent = parent;
            this.values = new HashMap<>(parent.getValues());
            this.values.put(position, value);
        }

        @Override
        public Map<Position, Integer> getValues() {
            return values;
        }

        @Override
        public boolean hasValue(Position position) {
            return values.containsKey(position);
        }

        @Override
        public OptionalInt getValue(Position position) {
            final Integer value = values.get(position);
            return value == null ? OptionalInt.empty() : OptionalInt.of(value);
        }

        @Override
        public Set<Position> getUnsolvedPositions() {
            if (unsolvedPositions == null) {
                unsolvedPositions = computeUnsolvedPositions();
            }
            return unsolvedPositions;
        }

        private Set<Position> computeUnsolvedPositions() {
            return parent.getUnsolvedPositions().stream()
                    .filter(p -> ! this.hasValue(p))
                    .collect(Collectors.toSet());
        }

        @Override
        public boolean isValid() {
            if (valid == null) {
                valid = computeValid();
            }
            return valid;
        }

        private boolean computeValid() {
            for (final Position position : Position.ALL_POSITIONS) {
                if (this.hasValue(position)) {
                    //This is set, so it shouldn't conflict with other cells:
                    if (getNeighbourValues(position).contains(this.getValueUnsafe(position))) {
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

        @Override
        public boolean isSolved() {
            return this.getValues().size() == Position.ALL_POSITIONS.size();
        }

        public SortedSet<Integer> getNeighbourValues(final Position position) {
            return this.neighbourValues.computeIfAbsent(position, this::computeNeighbourValues);
        }

        private SortedSet<Integer> computeNeighbourValues(final Position position) {
            return Position.getNeighbours(position).stream()
                    .filter(this::hasValue)
                    .map(this::getValueUnsafe)
                    .collect(Collectors.toCollection(TreeSet::new));
        }

        @Override
        public SortedSet<Integer> getPossibleValues(final Position position) {
            return this.possibleValues.computeIfAbsent(position, this::computePossibleValues);
        }

        private SortedSet<Integer> computePossibleValues(final Position position) {
            if (this.hasValue(position)) {
                return new TreeSet<>(List.of(this.getValueUnsafe(position)));
            } else {
                final TreeSet<Integer> potentialValues = new TreeSet<>(parent.getPossibleValues(position));
                potentialValues.removeAll(getNeighbourValues(position));
                return potentialValues;
            }
        }

    }

}
