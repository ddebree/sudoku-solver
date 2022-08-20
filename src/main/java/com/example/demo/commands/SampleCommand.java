package com.example.demo.commands;

import com.example.demo.exception.ValueAlreadySetException;
import com.example.demo.model.Position;
import com.example.demo.model.SudokuBoard;
import com.example.demo.model.Value;
import com.example.demo.service.BoardHolder;
import lombok.AllArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
@AllArgsConstructor
public class SampleCommand {

    private static final int[][] SAMPLE1 = {
            new int[] { 0, 0, 6, 0, 3, 1, 0, 7, 0 },
            new int[] { 4, 3, 7, 0, 0, 5, 0, 0, 0 },
            new int[] { 0, 1, 0, 4, 6, 7, 0, 0, 8 },
            new int[] { 0, 2, 9, 1, 7, 8, 3, 0, 0 },
            new int[] { 0, 0, 0, 0, 0, 0, 0, 2, 6 },
            new int[] { 3, 0, 0, 0, 5, 0, 0, 0, 0 },
            new int[] { 8, 0, 5, 0, 0, 4, 9, 1, 0 },
            new int[] { 0, 0, 3, 5, 0, 9, 0, 8, 7 },
            new int[] { 7, 9, 0, 0, 8, 6, 0, 0, 4 }
    };

    //20 Aug problem
    private static final int[][] SAMPLE2 = {
            new int[] { 0, 0, 0, /**/ 0, 0, 0, /**/ 0, 0, 6 },
            new int[] { 0, 9, 0, /**/ 3, 5, 0, /**/ 0, 7, 0 },
            new int[] { 0, 0, 0, /**/ 7, 0, 2, /**/ 0, 4, 0 },
            //---------------------------------------
            new int[] { 8, 0, 5, /**/ 0, 0, 0, /**/ 0, 0, 0 },
            new int[] { 0, 0, 0, /**/ 0, 0, 0, /**/ 7, 0, 0 },
            new int[] { 0, 0, 4, /**/ 0, 2, 6, /**/ 0, 0, 0 },
            //---------------------------------------
            new int[] { 0, 5, 8, /**/ 9, 0, 0, /**/ 0, 0, 0 },
            new int[] { 9, 0, 0, /**/ 0, 4, 0, /**/ 6, 0, 0 },
            new int[] { 4, 0, 0, /**/ 0, 7, 0, /**/ 0, 1, 0 }
    };

    private final BoardHolder boardHolder;

    @ShellMethod("Load the sample board")
    public void sample1() throws ValueAlreadySetException {
        load(SAMPLE1);
    }

    @ShellMethod("Load the sample board 2")
    public void sample2() throws ValueAlreadySetException {
        load(SAMPLE2);
    }

    private void load(int[][] data) throws ValueAlreadySetException {
        SudokuBoard board = SudokuBoard.empty();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int value = data[row][col];
                if (value > 0) {
                    board = board.withValue(Position.at(row, col), Value.values()[value - 1]);
                }
            }
        }
        boardHolder.setBoard(board);
    }

}
