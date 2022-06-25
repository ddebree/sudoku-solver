package com.example.demo.service;

import com.example.demo.model.SudokuBoard;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class BoardHolder {

    @Getter
    private SudokuBoard board = SudokuBoard.empty();

    public void setBoard(SudokuBoard board) {
        this.board = board;
    }

}
