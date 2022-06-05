package com.example.demo.model;

import lombok.Data;

import java.util.HashSet;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

@Data
public class Cell {

    private OptionalInt value = OptionalInt.empty();
    private final Set<Cell> neighbours = new HashSet<>();

    public void addNeighbour(Cell cell) {
        if (cell != this) {
            neighbours.add(cell);
        }
    }

    public void addNeighbours(Iterable<Cell> cells) {
        cells.forEach(this::addNeighbour);
    }

    public Set<Integer> getNeighbourValues() {
        final Set<Integer> values = new HashSet<>();
        for (final var neighbour : getNeighbours()) {
            if (neighbour.getValue().isPresent()) {
                values.add(neighbour.getValue().getAsInt());
            }
        }
        return values;
    }

}
