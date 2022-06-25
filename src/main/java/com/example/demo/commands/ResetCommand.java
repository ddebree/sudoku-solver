package com.example.demo.commands;

import com.example.demo.model.SudokuBoard;
import com.example.demo.service.BoardHolder;
import lombok.AllArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
@AllArgsConstructor
public class ResetCommand {

    private final BoardHolder boardHolder;

    @ShellMethod("Resets the board")
    public void reset() {
        boardHolder.setBoard(SudokuBoard.empty());
    }

}
