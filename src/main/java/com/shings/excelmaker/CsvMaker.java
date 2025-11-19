package com.shings.excelmaker;

import com.shings.excelmaker.exception.CsvException;
import com.shings.excelmaker.util.FileUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvMaker {
    private final Path archiveRootPath;
    private final Charset DEFAULT_CHARSET_CSV = Charset.forName("MS949");

    public CsvMaker(Path archiveRootPath) {
        if (archiveRootPath == null) {
            throw new CsvException("archiveRootPath must not be null.");
        }

        this.archiveRootPath = archiveRootPath.toAbsolutePath().normalize();
    }

    /**
     * Creates a CSV file where each element in the list becomes one row.
     *
     * @param lines    the lines to write into the CSV file.
     * @param filename the output CSV filename.
     * @return the generated CSV file.
     * @throws CsvException if CSV creation fails.
     */
    public File ofList(List<String> lines, String filename) throws CsvException {
        if (lines == null) {
            throw new CsvException("lines must not be null.");
        }

        if (FileUtil.isInvalidFilename(filename)) {
            throw new CsvException("Invalid file name: " + filename);
        }

        FileUtil.createDirectory(archiveRootPath);
        String safeFileName = filename.endsWith(".csv") ? filename : filename + ".csv";
        Path targetPath = archiveRootPath.resolve(safeFileName);

        try (BufferedWriter writer = Files.newBufferedWriter(targetPath, DEFAULT_CHARSET_CSV)) {
            for (String line : lines) {
                writer.append(line);
                writer.newLine();
            }

            writer.flush();

        } catch (IOException e) {
            throw new CsvException("Failed to write CSV file: " + targetPath, e);
        }

        return targetPath.toFile();
    }
}
