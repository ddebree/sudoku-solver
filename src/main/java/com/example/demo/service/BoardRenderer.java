package com.example.demo.service;

import com.example.demo.model.Position;
import com.example.demo.model.SudokuBoard;
import com.example.demo.model.Value;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.table.*;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Slf4j
public class BoardRenderer {

    private static final String BLANK = "";

    public void render(SudokuBoard board) {
        final TableModel model = new ArrayTableModel(Position.POSITIONS);
        final Table table = new TableBuilder(model)
                .addFullBorder(BorderStyle.oldschool)
                .on(CellMatchers.ofType(Position.class))
                .addFormatter(new SingleLineFormatter(board))
                .addAligner(SimpleHorizontalAligner.center)
                .addSizer(new AbsoluteWidthSizeConstraints(3))
                .build();

        log.info("Table:\n{}", table.render(80));
    }

    public void renderPossibles(SudokuBoard board) {
        final TableModel model = new ArrayTableModel(Position.POSITIONS);
        final Table table = new TableBuilder(model)
                .addFullBorder(BorderStyle.oldschool)
                .on(CellMatchers.ofType(Position.class))
                    .addFormatter(new TripleLineFormatter(board))
                    .addAligner(SimpleHorizontalAligner.center)
                    .addSizer(new AbsoluteWidthSizeConstraints(5))
                .on(CellMatchers.at(0, 0))
                .build();

        log.info("Table:\n{}", table.render(80));
    }

    @AllArgsConstructor
    private static abstract class CellFormatter implements Formatter {

        protected final SudokuBoard board;

        protected abstract String[] formatCell(Position position, Optional<Value> value);

        @Override
        public String[] format(final Object value) {
            if (value instanceof Position) {
                final Position position = (Position)value;
                final Optional<Value> cellValue = board.getValue(position);
                return formatCell(position, cellValue);
            }
            return new String[] { "" };
        }

    }

    private static class SingleLineFormatter extends CellFormatter {

        public SingleLineFormatter(SudokuBoard board) {
            super(board);
        }

        @Override
        public String[] formatCell(final Position position, Optional<Value> value) {
            return value
                    .map(v -> new String[]{ String.valueOf(v) })
                    .orElseGet(() -> new String[]{ BLANK });
        }
    }

    private static class TripleLineFormatter extends CellFormatter {

        public TripleLineFormatter(SudokuBoard board) {
            super(board);
        }

        @Override
        public String[] formatCell(final Position position, Optional<Value> value) {
            if (value.isPresent()) {
                return new String[] { BLANK, "<" + value.get() + ">", BLANK };
            } else {
                final EnumSet<Value> potentialValues = board.getPossibleValues(position);
                return new String[] {
                        getValue("123", potentialValues),
                        getValue("456", potentialValues),
                        getValue("789", potentialValues)
                };
            }
        }

        private String getValue(final String hash,
                                final EnumSet<Value> potentialValues) {
            return potentialValues.stream()
                    .map(Object::toString)
                    .filter(hash::contains)
                    .collect(Collectors.joining());
        }

    }

}
