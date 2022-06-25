package com.example.demo.service;

import com.example.demo.model.Cell;
import com.example.demo.model.SudokuBoard;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.table.*;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Slf4j
public class BoardRenderer {

    private static final String BLANK = "";

    public void render(SudokuBoard board) {
        final TableModel model = new ArrayTableModel(board.getCells());
        final Table table = new TableBuilder(model)
                .addFullBorder(BorderStyle.oldschool)
                .on(CellMatchers.ofType(Cell.class))
                .addFormatter(new SingleLineFormatter())
                .addAligner(SimpleHorizontalAligner.center)
                .addSizer(new AbsoluteWidthSizeConstraints(3))
                .build();

        log.info("Table:\n{}", table.render(80));
    }

    public void renderPossibles(SudokuBoard board) {
        final TableModel model = new ArrayTableModel(board.getCells());
        final Table table = new TableBuilder(model)
                .addFullBorder(BorderStyle.oldschool)
                .on(CellMatchers.ofType(Cell.class))
                    .addFormatter(new TripleLineFormatter(board))
                    .addAligner(SimpleHorizontalAligner.center)
                    .addSizer(new AbsoluteWidthSizeConstraints(5))
                .on(CellMatchers.at(0, 0))
                .build();

        log.info("Table:\n{}", table.render(80));
    }

    private static abstract class CellFormatter implements Formatter {

        protected abstract String[] formatCell(Cell cell);

        @Override
        public String[] format(final Object value) {
            if (value instanceof Cell) {
                final Cell cell = (Cell)value;
                return formatCell(cell);
            }
            return new String[] { "" };
        }

    }

    private static class SingleLineFormatter extends CellFormatter {

        @Override
        public String[] formatCell(final Cell cell) {
            if (cell.getValue().isPresent()) {
                return new String[] { String.valueOf(cell.getValue().getAsInt()) };
            } else {
                return new String[] { BLANK };
            }
        }
    }

    @AllArgsConstructor
    private static class TripleLineFormatter extends CellFormatter {

        private final SudokuBoard board;

        @Override
        public String[] formatCell(final Cell cell) {
            if (cell.getValue().isPresent()) {
                return new String[] { BLANK, "<" + cell.getValue().getAsInt() + ">", BLANK };
            } else {
                final Set<Integer> potentialValues = board.getPossibleValues(cell);
                return new String[] {
                        getValue("123", potentialValues),
                        getValue("456", potentialValues),
                        getValue("789", potentialValues)
                };
            }
        }

        private String getValue(final String hash,
                                final Set<Integer> potentialValues) {
            return potentialValues.stream()
                    .map(Object::toString)
                    .filter(hash::contains)
                    .collect(Collectors.joining());
        }

    }

}
