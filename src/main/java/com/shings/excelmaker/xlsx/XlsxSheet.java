package com.shings.excelmaker.xlsx;

import com.shings.excelmaker.exception.XlsxException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class XlsxSheet {
    private final String sheetName;
    private final List<XlsxSheetCell> headerCells;
    private final List<List<String>> rows;

    private XlsxSheet(Builder builder) {
        if (builder.rows == null) {
            throw new XlsxException("rows must not be null.");
        }

        this.sheetName = (builder.sheetName != null && !builder.sheetName.isBlank()) ? builder.sheetName : "data";
        this.headerCells = builder.headerCells;
        this.rows = builder.rows;
    }

    public static Builder builder(String sheetName) {
        return new Builder(sheetName);
    }

    public String getSheetName() {
        return sheetName;
    }

    public List<XlsxSheetCell> getHeaderCells() {
        return headerCells;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public boolean hasHeader() {
        return headerCells != null && !headerCells.isEmpty();
    }

    public static final class Builder {
        private final String sheetName;
        private List<XlsxSheetCell> headerCells;
        private List<List<String>> rows = new ArrayList<>();

        public Builder(String sheetName) {
            this.sheetName = sheetName;
        }

        public Builder header(List<String> headerTexts) {
            if (headerTexts == null) {
                throw new XlsxException("headerTexts must not be null.");
            }

            List<XlsxSheetCell> cells = new ArrayList<>();
            for (String title : headerTexts) {
                cells.add(XlsxSheetCell.builder(title).build());
            }

            this.headerCells = cells;
            return this;
        }

        public Builder headerStyled(List<XlsxSheetCell> headerCells) {
            if (headerCells == null) {
                throw new XlsxException("headerCells must not be null.");
            }

            if (headerCells.isEmpty()) {
                throw new XlsxException("headerCells must not be empty.");
            }

            this.headerCells = List.copyOf(headerCells);
            return this;
        }

        public Builder rows(List<List<String>> rows) {
            if (rows == null) {
                throw new XlsxException("rows must not be null.");
            }

            this.rows = List.copyOf(rows);
            return this;
        }

        public Builder lines(List<String> lines) {
            if (lines == null) {
                throw new XlsxException("lines must not be null.");
            }

            List<List<String>> converted = new ArrayList<>();
            for (String line : lines) {
                converted.add(Collections.singletonList(line));
            }

            this.rows = converted;
            return this;
        }

        public XlsxSheet build() {
            return new XlsxSheet(this);
        }
    }
}
