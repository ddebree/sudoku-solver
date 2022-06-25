package com.example.demo.commands;

import com.example.demo.model.SudokuBoard;
import com.example.demo.service.BoardHolder;
import com.example.demo.service.Solver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
@AllArgsConstructor
@Slf4j
public class CountCommand {

    private final BoardHolder boardHolder;
    private final Solver solver;

    @ShellMethod("Count the possible solutions")
    public void countSolutions() {
        SudokuBoard board = boardHolder.getBoard();
        log.info("Found {} solutions", solver.findSolutions(board).size());
    }

}
