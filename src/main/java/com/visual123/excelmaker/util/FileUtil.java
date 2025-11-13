package com.visual123.excelmaker.util;

public class FileUtil {
    private FileUtil() {
    }

    public static boolean isValidFilename(String filename) {
        if (filename == null) {
            return false;
        }

        String trimmedFilename = filename.trim();
        if (trimmedFilename.isEmpty()) {
            return false;
        }

        String invalidChars = "\\/:*?\"<>|";
        for (char c : invalidChars.toCharArray()) {
            if (trimmedFilename.indexOf(c) >= 0) {
                return false;
            }
        }

        return true;
    }

    public static boolean isInvalidFilename(String filename) {
        return !isValidFilename(filename);
    }
}
