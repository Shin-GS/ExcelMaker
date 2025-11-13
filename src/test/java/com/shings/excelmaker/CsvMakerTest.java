package com.shings.excelmaker;

import com.shings.excelmaker.exception.CsvMakerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for CsvMakerImpl to verify CSV file generation behavior.
 */
class CsvMakerImplTest {
    @TempDir
    Path tempDir;

    /**
     * Ensures that a CSV file is created successfully with correct content.
     */
    @Test
    void testCsvCreationSuccess() throws Exception {
        CsvMaker maker = new CsvMaker(tempDir);

        List<String> lines = List.of("A", "B", "C");
        File output = maker.ofList(lines, "test-file");

        assertTrue(output.exists(), "CSV file should be created.");
        assertTrue(output.getName().endsWith(".csv"), "File extension should be .csv");

        List<String> saved = Files.readAllLines(output.toPath());
        assertEquals(lines, saved, "CSV file content must match input lines.");
    }

    /**
     * Ensures IllegalArgumentException is thrown when null lines are provided.
     */
    @Test
    void testNullLinesThrowsException() {
        CsvMaker maker = new CsvMaker(tempDir);

        assertThrows(CsvMakerException.class, () ->
                maker.ofList(null, "test.csv"));
    }

    /**
     * Ensures IllegalArgumentException is thrown when an invalid filename is provided.
     */
    @Test
    void testInvalidFilenameThrowsException() {
        CsvMaker maker = new CsvMaker(tempDir);

        assertThrows(CsvMakerException.class, () ->
                maker.ofList(List.of("A"), "invalid/name"));
    }

    /**
     * Ensures the archive directory is created automatically if it does not exist.
     */
    @Test
    void testDirectoryAutoCreation() {
        Path customDir = tempDir.resolve("nested/folder/structure");
        CsvMaker maker = new CsvMaker(customDir);

        File output = maker.ofList(List.of("A"), "created");

        assertTrue(Files.exists(customDir), "Archive directory should be created automatically.");
        assertTrue(output.exists(), "CSV file must be created inside the generated directory.");
    }
}
