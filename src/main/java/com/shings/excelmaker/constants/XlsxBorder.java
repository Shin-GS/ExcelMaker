package com.shings.excelmaker.constants;

import org.apache.poi.ss.usermodel.BorderStyle;

public enum XlsxBorder {
    NONE(BorderStyle.NONE),
    THIN(BorderStyle.THIN),
    MEDIUM(BorderStyle.MEDIUM),
    THICK(BorderStyle.THICK);

    private final BorderStyle poiBorder;

    XlsxBorder(BorderStyle poiBorder) {
        this.poiBorder = poiBorder;
    }

    public BorderStyle toPoiBorder() {
        return poiBorder;
    }
}
