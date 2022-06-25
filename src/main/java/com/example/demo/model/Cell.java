package com.example.demo.model;

import lombok.Data;

import java.util.OptionalInt;

@Data
public class Cell {

    private final int row;
    private final int col;
    private final OptionalInt value;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.value = OptionalInt.empty();
    }

    public Cell(int row, int col, int value) {
        this.row = row;
        this.col = col;
        this.value = OptionalInt.of(value);
    }
}
