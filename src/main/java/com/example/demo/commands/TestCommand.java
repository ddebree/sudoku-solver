package com.example.demo.commands;

import com.example.demo.exception.ConflictingNeighbourValueException;
import com.example.demo.exception.ValueAlreadySetException;
import com.example.demo.model.SudokuBoard;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class TestCommand {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_GRAY = "\u001B[37m";
    public static final String ANSI_WHITE = "\u001B[37;1m";

    @ShellMethod("Add two integers together.")
    public int add(int a, int b) {
        try {
            System.out.println(ANSI_RED + "red text" + ANSI_RESET);
            System.out.println(SudokuBoard.empty()
                            .withValue(2,3, 8)
                            .toString());
        } catch (ValueAlreadySetException | ConflictingNeighbourValueException e) {
            throw new RuntimeException(e);
        }

        return a + b;
    }

}
