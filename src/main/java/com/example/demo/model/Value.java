package com.example.demo.model;

public enum Value {

    VALUE_1,
    VALUE_2,
    VALUE_3,
    VALUE_4,
    VALUE_5,
    VALUE_6,
    VALUE_7,
    VALUE_8,
    VALUE_9;

    @Override
    public String toString() {
        return String.valueOf(this.ordinal() + 1);
    }
}
