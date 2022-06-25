package com.example.demo.commands;

import com.example.demo.model.SudokuBoard;
import com.example.demo.service.BoardHolder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
@AllArgsConstructor
@Slf4j
public class CountCommand {

    private final BoardHolder boardHolder;

    @ShellMethod("Count the possible solutions")
    public void countSolutions() {
        SudokuBoard board = boardHolder.getBoard();
        log.info("Found {} solutions", board.countSolutions());
    }

}
