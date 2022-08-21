package com.example.demo.model;

import com.example.demo.exception.ValueAlreadySetException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class SudokuBoard {

    protected abstract Map<Position, Value> getValues();

    public abstract Optional<Value> getValue(Position position);

    public abstract Set<Position> getUnsolvedPositions();

    public abstract EnumSet<Value> getPossibleValues(final Position position);

    public abstract boolean isSolved();

    public abstract boolean isValid();

    public static SudokuBoard empty() {
        return EmptySudokuBoard.INSTANCE;
    }

    public SudokuBoard withValue(final Position position, final Value value) throws ValueAlreadySetException {
        final Optional<Value> currentValue = getValue(position);
        if (currentValue.isPresent()) {
            if (currentValue.get() == value) {
                return this;
            } else {
                throw new ValueAlreadySetException();
            }
        }
        return new ChildSudokuBoard(this, position, value);
    }

    private static class EmptySudokuBoard extends SudokuBoard {

        private static final SudokuBoard INSTANCE = new EmptySudokuBoard();

        @Override
        public Map<Position, Value> getValues() {
            return Collections.emptyMap();
        }

        @Override
        public Optional<Value> getValue(Position position) {
            return Optional.empty();
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

        public EnumSet<Value> getPossibleValues(final Position position) {
            return EnumSet.allOf(Value.class);
        }

    }

    private static class ChildSudokuBoard extends SudokuBoard {

        private final SudokuBoard parent;

        private final Map<Position, Value> values;
        private transient Boolean valid = null;
        private transient Set<Position> unsolvedPositions = null;
        private final Map<Position, EnumSet<Value>> neighbourValues = new ConcurrentHashMap<>();
        private final Map<Position, EnumSet<Value>> possibleValues = new ConcurrentHashMap<>();

        private ChildSudokuBoard(SudokuBoard parent, Position position, Value value) {
            this.parent = parent;
            this.values = new HashMap<>(parent.getValues());
            this.values.put(position, value);
        }

        @Override
        public Map<Position, Value> getValues() {
            return values;
        }

        @Override
        public Optional<Value> getValue(Position position) {
            return Optional.ofNullable(values.get(position));
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
                    .filter(p -> ! values.containsKey(p))
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
                Optional<Value> value = getValue(position);
                if (value.isPresent()) {
                    //This is set, so it shouldn't conflict with other cells:
                    if (getNeighbourValues(position).contains(value.get())) {
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

        public EnumSet<Value> getNeighbourValues(final Position position) {
            return this.neighbourValues.computeIfAbsent(position, this::computeNeighbourValues);
        }

        private EnumSet<Value> computeNeighbourValues(final Position position) {
            return Position.getNeighbours(position).stream()
                    .map(this::getValue)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(Value.class)));
        }

        @Override
        public EnumSet<Value> getPossibleValues(final Position position) {
            return this.possibleValues.computeIfAbsent(position, this::computePossibleValues);
        }

        private EnumSet<Value> computePossibleValues(final Position position) {
            final Optional<Value> value = getValue(position);
            if (value.isPresent()) {
                return EnumSet.of(value.get());
            } else {
                final EnumSet<Value> potentialValues = EnumSet.copyOf(parent.getPossibleValues(position));
                potentialValues.removeAll(getNeighbourValues(position));
                return potentialValues;
            }
        }

    }

}
