package com.example.demo.model;

import lombok.Data;

import java.util.OptionalInt;

@Data
public class Cell {

    private final Position position;
    private final OptionalInt value;

    public Cell(Position position) {
        this.position = position;
        this.value = OptionalInt.empty();
    }

    public Cell(Position position, int value) {
        this.position = position;
        this.value = OptionalInt.of(value);
    }
}
