package com.shings.excelmaker.constants;

import org.apache.poi.ss.usermodel.HorizontalAlignment;

public enum XlsxTextAlign {
    DEFAULT(null),
    LEFT(HorizontalAlignment.LEFT),
    CENTER(HorizontalAlignment.CENTER),
    RIGHT(HorizontalAlignment.RIGHT);

    private final HorizontalAlignment poiAlignment;

    XlsxTextAlign(HorizontalAlignment poiAlignment) {
        this.poiAlignment = poiAlignment;
    }

    public HorizontalAlignment toPoiAlignment() {
        return poiAlignment;
    }
}
