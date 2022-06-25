package com.example.demo.commands;

import com.example.demo.service.BoardHolder;
import com.example.demo.service.BoardRenderer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
@AllArgsConstructor
@Slf4j
public class ShowCommand {

    private final BoardHolder boardHolder;
    private final BoardRenderer boardRenderer;

    @ShellMethod("Show the sudoku board")
    public void show() {
        boardRenderer.render(boardHolder.getBoard());
    }

    @ShellMethod("Show the possible sudoku board")
    public void showPossible() {
        boardRenderer.renderPossibles(boardHolder.getBoard());
    }

}
