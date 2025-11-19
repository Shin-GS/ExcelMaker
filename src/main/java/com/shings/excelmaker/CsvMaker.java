package com.shings.excelmaker;

import com.shings.excelmaker.exception.CsvException;
import com.shings.excelmaker.util.CollectionCopyUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CsvMaker {
    private final String fileName;
    private final List<String> lines;
    private final List<List<String>> rows;
    private final char delimiter;
    private final String lineSeparator;

    private CsvMaker(Builder builder) {
        if (builder.fileName == null || builder.fileName.isBlank()) {
            throw new CsvException("fileName must not be null or blank.");
        }

        this.fileName = builder.fileName;
        this.lines = CollectionCopyUtils.nullSafeCopyOf(builder.lines);
        this.rows = CollectionCopyUtils.nullSafeCopyOf(builder.rows);
        this.delimiter = builder.delimiter;
        this.lineSeparator = builder.lineSeparator;
    }

    public static Builder builder(String fileName) {
        return new Builder(fileName);
    }

    public String getFileName() {
        return fileName;
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

    public byte[] toBytes() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            generate(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new CsvException("Failed to convert CSV content to byte array.", e);
        }
    }

    public void write(OutputStream out) {
        if (out == null) {
            throw new CsvException("OutputStream must not be null.");
        }

        generate(out);
    }

    public Path toPath(Path targetPath) {
        if (targetPath == null) {
            throw new CsvException("targetPath must not be null.");
        }

        try (OutputStream outputStream = Files.newOutputStream(targetPath)) {
            generate(outputStream);
            return targetPath;

        } catch (IOException e) {
            throw new CsvException("Failed to write CSV file: " + targetPath, e);
        }
    }

    public File toFile(File targetFile) {
        if (targetFile == null) {
            throw new CsvException("targetFile must not be null.");
        }

        toPath(targetFile.toPath());
        return targetFile;
    }

    public File toFile(Path dir, String fileName) {
        if (dir == null) {
            throw new CsvException("dir must not be null.");
        }

        if (fileName == null || fileName.isBlank()) {
            throw new CsvException("fileName must not be null or blank.");
        }

        Path resolved = dir.resolve(fileName);
        toPath(resolved);
        return resolved.toFile();
    }

    public File toTempFile() {
        try {
            Path temp = Files.createTempFile(null, ".csv");
            try (OutputStream out = Files.newOutputStream(temp)) {
                generate(out);
            }
            return temp.toFile();

        } catch (IOException e) {
            throw new CsvException("Failed to generate temporary CSV file.", e);
        }
    }

    private void generate(OutputStream out) {
        try {
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

        } catch (IOException e) {
            throw new CsvException("Failed to generate CSV content.", e);
        }
    }

    private String encodeCell(String value) {
        if (value == null) {
            return "";
        }

        boolean needsQuote = value.indexOf(delimiter) >= 0 || value.indexOf('"') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0;
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

        public Builder lines(List<String> lineList) {
            if (lineList == null) {
                throw new CsvException("lines must not be null.");
            }

            if (!lineList.isEmpty()) {
                lines.addAll(CollectionCopyUtils.nullSafeCopyOf(lineList));
            }

            return this;
        }

        public Builder row(List<String> row) {
            if (row != null) {
                rows.add(row);
            }

            return this;
        }

        public Builder rows(List<List<String>> rowList) {
            if (rowList == null) {
                throw new CsvException("rows must not be null.");
            }

            if (!rowList.isEmpty()) {
                rows.addAll(CollectionCopyUtils.nullSafeCopyOf(rowList));
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
