package com.example.demo.commands;

import com.example.demo.model.SudokuBoard;
import com.example.demo.service.BoardHolder;
import com.example.demo.service.BoardRenderer;
import com.example.demo.service.Solver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.Set;

@ShellComponent
@AllArgsConstructor
@Slf4j
public class SolveCommand {

    private final BoardHolder boardHolder;
    private final Solver solver;
    private final BoardRenderer boardRenderer;

    @ShellMethod("Solve the possible board")
    public void solveBoard() {
        final SudokuBoard board = boardHolder.getBoard();
        final Set<SudokuBoard> solutions = solver.findSolutions(board);
        if (solutions.size() < 10) {
            for (SudokuBoard sboard : solutions) {
                boardRenderer.render(sboard);
            }
        } else {
            log.warn("Found {} solutions", solutions.size());
        }
    }

}
