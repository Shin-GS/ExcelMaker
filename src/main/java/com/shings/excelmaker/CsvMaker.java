package com.shings.excelmaker;

import com.shings.excelmaker.exception.CsvException;
import com.shings.excelmaker.util.CollectionCopyUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class CsvMaker extends AbstractMaker<CsvException> {
    private final List<String> lines;
    private final List<List<String>> rows;
    private final char delimiter;
    private final String lineSeparator;

    private CsvMaker(Builder builder) {
        super(builder.fileName);
        this.lines = CollectionCopyUtils.nullSafeCopyOf(builder.lines);
        this.rows = CollectionCopyUtils.nullSafeCopyOf(builder.rows);
        this.delimiter = builder.delimiter;
        this.lineSeparator = builder.lineSeparator;
    }

    public static Builder builder(String fileName) {
        return new Builder(fileName);
    }

    public List<String> getLines() {
        return lines;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public String getLineSeparator() {
        return lineSeparator;
    }

    public File toTempFile() {
        return toTempFile(".csv");
    }

    @Override
    protected void generate(OutputStream out) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        for (String line : lines) {
            writer.write(line);
            writer.write(lineSeparator);
        }

        for (List<String> row : rows) {
            if (row == null || row.isEmpty()) {
                writer.write(lineSeparator);
                continue;
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < row.size(); i++) {
                if (i > 0) {
                    sb.append(delimiter);
                }
                sb.append(encodeCell(row.get(i)));
            }

            writer.write(sb.toString());
            writer.write(lineSeparator);
        }

        writer.flush();
    }

    @Override
    protected CsvException createException(String message, Throwable cause) {
        if (cause == null) {
            return new CsvException(message);
        }

        return new CsvException(message, cause);
    }

    private String encodeCell(String value) {
        if (value == null) {
            return "";
        }

        boolean needsQuote =
                value.indexOf(delimiter) >= 0
                        || value.indexOf('"') >= 0
                        || value.indexOf('\n') >= 0
                        || value.indexOf('\r') >= 0;
        if (!needsQuote) {
            return value;
        }

        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"') {
                sb.append('"');
            }
            sb.append(c);
        }
        sb.append('"');
        return sb.toString();
    }

    public static final class Builder {
        private final String fileName;
        private final List<String> lines = new ArrayList<>();
        private final List<List<String>> rows = new ArrayList<>();
        private char delimiter = ',';
        private String lineSeparator = System.lineSeparator();

        public Builder(String fileName) {
            this.fileName = fileName;
        }

        public Builder line(String line) {
            if (line != null) {
                lines.add(line);
            }

            return this;
        }

        public Builder lines(List<String> lines) {
            if (lines == null) {
                throw new CsvException("lines must not be null.");
            }

            if (!lines.isEmpty()) {
                this.lines.addAll(CollectionCopyUtils.nullSafeCopyOf(lines));
            }

            return this;
        }

        public Builder row(List<String> row) {
            if (row != null) {
                rows.add(row);
            }

            return this;
        }

        public Builder rows(List<List<String>> rows) {
            if (rows == null) {
                throw new CsvException("rows must not be null.");
            }

            if (!rows.isEmpty()) {
                this.rows.addAll(CollectionCopyUtils.nullSafeCopyOf(rows));
            }

            return this;
        }

        public Builder delimiter(char delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public Builder lineSeparator(String lineSeparator) {
            if (lineSeparator == null) {
                throw new CsvException("lineSeparator must not be null.");
            }

            this.lineSeparator = lineSeparator;
            return this;
        }

        public CsvMaker build() {
            return new CsvMaker(this);
        }
    }
}
