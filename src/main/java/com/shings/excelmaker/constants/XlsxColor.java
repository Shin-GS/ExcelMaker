package com.shings.excelmaker.constants;

import org.apache.poi.ss.usermodel.IndexedColors;

public enum XlsxColor {
    BLACK(IndexedColors.BLACK),
    WHITE(IndexedColors.WHITE),
    RED(IndexedColors.RED),
    BLUE(IndexedColors.BLUE),
    GREEN(IndexedColors.GREEN),
    YELLOW(IndexedColors.YELLOW),
    GREY(IndexedColors.GREY_50_PERCENT),
    ORANGE(IndexedColors.ORANGE);

    private final IndexedColors poiColor;

    XlsxColor(IndexedColors poiColor) {
        this.poiColor = poiColor;
    }

    public Short toPoiColorIndex() {
        return poiColor.getIndex();
    }
}
