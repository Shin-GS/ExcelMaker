package com.shings.excelmaker;

import com.shings.excelmaker.exception.CsvException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvMakerTest {
    @TempDir
    Path tempDir;

    @Test
    void builder_nullFileName_throwsExceptionOnBuild() {
        CsvMaker.Builder builder = CsvMaker.builder(null);

        assertThrows(CsvException.class, builder::build);
    }

    @Test
    void builder_blankFileName_throwsExceptionOnBuild() {
        CsvMaker.Builder builder = CsvMaker.builder("   ");

        assertThrows(CsvException.class, builder::build);
    }

    @Test
    void builder_lines_nullList_throwsException() {
        CsvMaker.Builder builder = CsvMaker.builder("test.csv");

        assertThrows(CsvException.class, () -> builder.lines(null));
    }

    @Test
    void builder_rows_nullList_throwsException() {
        CsvMaker.Builder builder = CsvMaker.builder("test.csv");

        assertThrows(CsvException.class, () -> builder.rows(null));
    }

    @Test
    void builder_line_null_isIgnored() {
        CsvMaker maker = CsvMaker.builder("test.csv")
                .line(null)
                .build();

        assertNotNull(maker.getRows());
        assertTrue(maker.getRows().isEmpty());
    }

    @Test
    void builder_lines_addsSingleColumnRows() {
        List<String> lines = List.of("L1", "L2");

        CsvMaker maker = CsvMaker.builder("test.csv")
                .lines(lines)
                .build();

        List<List<String>> rows = maker.getRows();
        assertEquals(2, rows.size());
        assertEquals(List.of("L1"), rows.get(0));
        assertEquals(List.of("L2"), rows.get(1));
    }

    @Test
    void builder_rows_listWithNullRow_filtersNullRows() {
        List<String> row1 = List.of("A", "B");
        List<String> row2 = List.of("C", "D");

        List<List<String>> rows = new ArrayList<>();
        rows.add(row1);
        rows.add(null);
        rows.add(row2);

        CsvMaker maker = CsvMaker.builder("rows.csv")
                .rows(rows)
                .build();

        assertEquals(2, maker.getRows().size());
        assertEquals(row1, maker.getRows().get(0));
        assertEquals(row2, maker.getRows().get(1));
    }

    @Test
    void write_nullOutputStream_throwsException() {
        CsvMaker maker = CsvMaker.builder("test.csv").build();

        assertThrows(CsvException.class, () -> maker.write(null));
    }

    @Test
    void toPath_nullTargetPath_throwsException() {
        CsvMaker maker = CsvMaker.builder("test.csv").build();

        assertThrows(CsvException.class, () -> maker.toPath(null));
    }

    @Test
    void toFile_nullTargetFile_throwsException() {
        CsvMaker maker = CsvMaker.builder("test.csv").build();

        assertThrows(CsvException.class, () -> maker.toFile(null));
    }

    @Test
    void toFile_dirNull_throwsException() {
        CsvMaker maker = CsvMaker.builder("test.csv").build();

        assertThrows(CsvException.class, () -> maker.toFile(null, "file.csv"));
    }

    @Test
    void toFile_blankFileName_throwsException() {
        CsvMaker maker = CsvMaker.builder("test.csv").build();

        assertThrows(CsvException.class, () -> maker.toFile(tempDir, "   "));
    }

    @Test
    void toBytes_withLinesAndRows_generatesExpectedCsv() {
        List<String> headerLines = List.of("HEADER_ROW1", "HEADER_ROW2");
        List<List<String>> rows = List.of(
                List.of("A1", "B1"),
                List.of("A2", "B2")
        );

        CsvMaker maker = CsvMaker.builder("test.csv")
                .lines(headerLines)
                .rows(rows)
                .lineSeparator("\n")
                .build();

        byte[] bytes = maker.toBytes();

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        String csv = new String(bytes, StandardCharsets.UTF_8);
        String expected =
                """
                        HEADER_ROW1
                        HEADER_ROW2
                        A1,B1
                        A2,B2
                        """;

        assertEquals(expected, csv);
    }

    @Test
    void toBytes_withSpecialCharacters_quotesAndEscapesProperly() {
        List<List<String>> rows = List.of(
                List.of("a,b", "text \"with\" quotes", "line1\nline2")
        );

        CsvMaker maker = CsvMaker.builder("special.csv")
                .rows(rows)
                .lineSeparator("\n")
                .build();

        byte[] bytes = maker.toBytes();

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        String csv = new String(bytes, StandardCharsets.UTF_8);

        String expected =
                "\"a,b\",\"text \"\"with\"\" quotes\",\"line1\nline2\"\n";

        assertEquals(expected, csv);
    }

    @Test
    void toBytes_withCustomDelimiter_generatesExpectedCsv() {
        List<List<String>> rows = List.of(
                List.of("A", "B", "C")
        );

        CsvMaker maker = CsvMaker.builder("semicolon.csv")
                .rows(rows)
                .delimiter(';')
                .lineSeparator("\n")
                .build();

        byte[] bytes = maker.toBytes();

        String csv = new String(bytes, StandardCharsets.UTF_8);
        String expected = "A;B;C\n";

        assertEquals(expected, csv);
    }

    @Test
    void toBytes_withNoLinesAndNoRows_createsEmptyContent() {
        CsvMaker maker = CsvMaker.builder("empty.csv")
                .lineSeparator("\n")
                .build();

        byte[] bytes = maker.toBytes();

        assertNotNull(bytes);
        String csv = new String(bytes, StandardCharsets.UTF_8);
        assertEquals("", csv);
    }

    @Test
    void toPath_writesFileOnDisk() throws IOException {
        List<List<String>> rows = List.of(
                List.of("C1", "D1")
        );

        CsvMaker maker = CsvMaker.builder("data.csv")
                .rows(rows)
                .lineSeparator("\n")
                .build();

        Path targetPath = tempDir.resolve("data.csv");

        maker.toPath(targetPath);

        assertTrue(Files.exists(targetPath));
        assertTrue(Files.size(targetPath) > 0L);

        String content = Files.readString(targetPath, StandardCharsets.UTF_8);
        assertEquals("C1,D1\n", content);
    }

    @Test
    void toFile_writesFileOnDisk() throws IOException {
        List<List<String>> rows = List.of(
                List.of("X1", "Y1")
        );

        CsvMaker maker = CsvMaker.builder("file.csv")
                .rows(rows)
                .lineSeparator("\n")
                .build();

        Path targetPath = tempDir.resolve("file.csv");
        File targetFile = targetPath.toFile();

        maker.toFile(targetFile);

        assertTrue(targetFile.exists());
        assertTrue(targetFile.length() > 0L);

        String content = Files.readString(targetPath, StandardCharsets.UTF_8);
        assertEquals("X1,Y1\n", content);
    }

    @Test
    void toFile_withDirAndFileName_writesFileOnDisk() throws IOException {
        List<List<String>> rows = List.of(
                List.of("D1", "E1")
        );

        CsvMaker maker = CsvMaker.builder("ignored.csv")
                .rows(rows)
                .lineSeparator("\n")
                .build();

        String fileName = "dir-file.csv";

        File resultFile = maker.toFile(tempDir, fileName);

        assertNotNull(resultFile);
        assertTrue(resultFile.exists());
        assertTrue(resultFile.length() > 0L);
        assertEquals(tempDir.resolve(fileName), resultFile.toPath());

        String content = Files.readString(resultFile.toPath(), StandardCharsets.UTF_8);
        assertEquals("D1,E1\n", content);
    }

    @Test
    void toTempFile_createsPhysicalTempFile() throws IOException {
        List<List<String>> rows = List.of(
                List.of("T1", "T2")
        );

        CsvMaker maker = CsvMaker.builder("temp.csv")
                .rows(rows)
                .lineSeparator("\n")
                .build();

        File tempFile = maker.toTempFile();

        assertNotNull(tempFile);
        assertTrue(tempFile.exists());
        assertTrue(tempFile.length() > 0L);

        String content = Files.readString(tempFile.toPath(), StandardCharsets.UTF_8);
        assertEquals("T1,T2\n", content);
    }

    @Test
    void write_writesToProvidedOutputStream() throws IOException {
        List<List<String>> rows = List.of(
                List.of("O1", "P1")
        );

        CsvMaker maker = CsvMaker.builder("out.csv")
                .rows(rows)
                .lineSeparator("\n")
                .build();

        Path targetPath = tempDir.resolve("out.csv");

        try (OutputStream os = Files.newOutputStream(targetPath)) {
            maker.write(os);
        }

        assertTrue(Files.exists(targetPath));
        assertTrue(Files.size(targetPath) > 0L);

        String content = Files.readString(targetPath, StandardCharsets.UTF_8);
        assertEquals("O1,P1\n", content);
    }

    @Test
    void getFileNameAndProperties_returnValuesFromBuilder() {
        CsvMaker maker = CsvMaker.builder("named.csv")
                .delimiter(';')
                .lineSeparator("\n")
                .build();

        assertEquals("named.csv", maker.getFileName());
        assertEquals(';', maker.getDelimiter());
        assertEquals("\n", maker.getLineSeparator());
    }

    @Test
    void toBytes_usesUtf8Encoding() {
        List<List<String>> rows = List.of(
                List.of("한글", "テスト")
        );

        CsvMaker maker = CsvMaker.builder("utf8.csv")
                .rows(rows)
                .lineSeparator("\n")
                .build();

        byte[] bytes = maker.toBytes();

        String csv = new String(bytes, StandardCharsets.UTF_8);
        assertEquals("한글,テスト\n", csv);
    }

    @Test
    void generate_writesEmptyLineForEmptyRow() {
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("A"));
        rows.add(List.of());
        rows.add(List.of("B"));

        CsvMaker maker = CsvMaker.builder("empty-row.csv")
                .rows(rows)
                .lineSeparator("\n")
                .build();

        byte[] bytes = maker.toBytes();
        String csv = new String(bytes, StandardCharsets.UTF_8);

        String expected =
                """
                        A
                        
                        B
                        """;

        assertEquals(expected, csv);
    }

    @Test
    void toBytes_usesSystemLineSeparatorByDefault() {
        List<List<String>> rows = List.of(
                List.of("A", "B"),
                List.of("C", "D")
        );

        CsvMaker maker = CsvMaker.builder("default-ls.csv")
                .rows(rows)
                .build();

        byte[] bytes = maker.toBytes();
        String csv = new String(bytes, StandardCharsets.UTF_8);

        String ls = System.lineSeparator();
        String expected =
                "A,B" + ls +
                        "C,D" + ls;

        assertEquals(expected, csv);
    }
}
