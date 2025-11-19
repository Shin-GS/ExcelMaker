package com.shings.excelmaker.util;

import java.util.ArrayList;
import java.util.List;

public final class CollectionCopyUtils {
    private CollectionCopyUtils() {
    }

    public static <T> List<T> nullSafeCopyOf(List<T> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }

        List<T> result = new ArrayList<>();
        for (T element : source) {
            if (element != null) {
                result.add(element);
            }
        }

        return List.copyOf(result);
    }
}
