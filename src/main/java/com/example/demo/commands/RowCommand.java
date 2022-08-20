package com.example.demo.commands;

import com.example.demo.model.Position;
import com.example.demo.model.Value;
import com.example.demo.service.BoardHolder;
import com.example.demo.service.BoardRenderer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
@AllArgsConstructor
@Slf4j
public class RowCommand {

    private final BoardHolder boardHolder;
    private final BoardRenderer boardRenderer;

    @SneakyThrows
    @ShellMethod("Set a row value")
    public void row(int row, String rowValue) {
        var board = this.boardHolder.getBoard();
        if (rowValue.length() <= 9) {
            int col = 0;
            for (Character c : rowValue.toCharArray()) {
                int value = c - '1';
                if (value > 0 && value <= 9) {
                    board = board.withValue(Position.at(row, col), Value.values()[value]);
                }
                col++;
            }
        } else {
            log.info("Row value is greater than 9 characters long: '{}'", rowValue);
        }
        boardHolder.setBoard(board);
        boardRenderer.render(board);
    }

}
