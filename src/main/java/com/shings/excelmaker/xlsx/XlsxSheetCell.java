package com.shings.excelmaker.xlsx;

import com.shings.excelmaker.xlsx.attribute.XlsxBorder;
import com.shings.excelmaker.xlsx.attribute.XlsxColor;
import com.shings.excelmaker.xlsx.attribute.XlsxTextAlign;
import com.shings.excelmaker.exception.XlsxException;

public final class XlsxSheetCell {
    private static final int EXCEL_MAX_COLUMN_WIDTH = 255 * 256;

    private final String text;
    private final Integer columnWidth;
    private final XlsxColor backgroundColor;
    private final XlsxColor fontColor;
    private final XlsxTextAlign horizontalAlignment;
    private final XlsxBorder border;

    private XlsxSheetCell(Builder builder) {
        if (builder.columnWidth != null) {
            if (builder.columnWidth <= 0) {
                throw new XlsxException("columnWidth must be greater than 0.");
            }

            if (builder.columnWidth > EXCEL_MAX_COLUMN_WIDTH) {
                throw new XlsxException("columnWidth must be less than or equal to " + EXCEL_MAX_COLUMN_WIDTH + ".");
            }
        }

        this.text = builder.text != null ? builder.text : "";
        this.columnWidth = builder.columnWidth;
        this.backgroundColor = builder.backgroundColor != null ? builder.backgroundColor : null;
        this.fontColor = builder.fontColor != null ? builder.fontColor : null;
        this.horizontalAlignment = builder.horizontalAlignment != null ? builder.horizontalAlignment : null;
        this.border = builder.border != null ? builder.border : null;
    }

    public String getText() {
        return text;
    }

    public Integer getColumnWidth() {
        return columnWidth;
    }

    public XlsxColor getBackgroundColor() {
        return backgroundColor;
    }

    public XlsxColor getFontColor() {
        return fontColor;
    }

    public XlsxTextAlign getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public XlsxBorder getBorder() {
        return border;
    }

    public static Builder builder(String text) {
        return new Builder(text);
    }

    public static final class Builder {
        private final String text;
        private Integer columnWidth;
        private XlsxColor backgroundColor;
        private XlsxColor fontColor;
        private XlsxTextAlign horizontalAlignment;
        private XlsxBorder border;

        public Builder(String text) {
            this.text = text;
        }

        public Builder columnWidth(int columnWidth) {
            this.columnWidth = columnWidth;
            return this;
        }

        public Builder backgroundColor(XlsxColor color) {
            this.backgroundColor = color;
            return this;
        }

        public Builder fontColor(XlsxColor color) {
            this.fontColor = color;
            return this;
        }

        public Builder horizontalAlignment(XlsxTextAlign alignment) {
            this.horizontalAlignment = alignment;
            return this;
        }

        public Builder border(XlsxBorder border) {
            this.border = border;
            return this;
        }

        public XlsxSheetCell build() {
            return new XlsxSheetCell(this);
        }
    }
}
