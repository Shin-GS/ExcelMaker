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

class CsvCreatorTest {
    @TempDir
    Path tempDir;

    @Test
    void builder_nullFileName_throwsExceptionOnBuild() {
        CsvCreator.Builder builder = CsvCreator.builder(null);

        assertThrows(CsvException.class, builder::build);
    }

    @Test
    void builder_blankFileName_throwsExceptionOnBuild() {
        CsvCreator.Builder builder = CsvCreator.builder("   ");

        assertThrows(CsvException.class, builder::build);
    }

    @Test
    void builder_lines_nullList_throwsException() {
        CsvCreator.Builder builder = CsvCreator.builder("test.csv");

        assertThrows(CsvException.class, () -> builder.lines(null));
    }

    @Test
    void builder_rows_nullList_throwsException() {
        CsvCreator.Builder builder = CsvCreator.builder("test.csv");

        assertThrows(CsvException.class, () -> builder.rows(null));
    }

    @Test
    void builder_line_null_isIgnored() {
        CsvCreator creator = CsvCreator.builder("test.csv")
                .line(null)
                .build();

        assertNotNull(creator.getLines());
        assertTrue(creator.getLines().isEmpty());
    }

    @Test
    void builder_rows_listWithNullRow_filtersNullRows() {
        List<String> row1 = List.of("A", "B");
        List<String> row2 = List.of("C", "D");

        List<List<String>> rows = new ArrayList<>();
        rows.add(row1);
        rows.add(null);
        rows.add(row2);

        CsvCreator creator = CsvCreator.builder("rows.csv")
                .rows(rows)
                .build();

        assertEquals(2, creator.getRows().size());
        assertEquals(row1, creator.getRows().get(0));
        assertEquals(row2, creator.getRows().get(1));
    }

    @Test
    void write_nullOutputStream_throwsException() {
        CsvCreator creator = CsvCreator.builder("test.csv").build();

        assertThrows(CsvException.class, () -> creator.write(null));
    }

    @Test
    void toPath_nullTargetPath_throwsException() {
        CsvCreator creator = CsvCreator.builder("test.csv").build();

        assertThrows(CsvException.class, () -> creator.toPath(null));
    }

    @Test
    void toFile_nullTargetFile_throwsException() {
        CsvCreator creator = CsvCreator.builder("test.csv").build();

        assertThrows(CsvException.class, () -> creator.toFile((File) null));
    }

    @Test
    void toFile_dirNull_throwsException() {
        CsvCreator creator = CsvCreator.builder("test.csv").build();

        assertThrows(CsvException.class, () -> creator.toFile(null, "file.csv"));
    }

    @Test
    void toFile_blankFileName_throwsException() {
        CsvCreator creator = CsvCreator.builder("test.csv").build();

        assertThrows(CsvException.class, () -> creator.toFile(tempDir, "   "));
    }

    @Test
    void toBytes_withLinesAndRows_generatesExpectedCsv() {
        List<String> headerLines = List.of("HEADER1,HEADER2");
        List<List<String>> rows = List.of(
                List.of("A1", "B1"),
                List.of("A2", "B2")
        );

        CsvCreator creator = CsvCreator.builder("test.csv")
                .lines(headerLines)
                .rows(rows)
                .lineSeparator("\n")
                .build();

        byte[] bytes = creator.toBytes();

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        String csv = new String(bytes, StandardCharsets.UTF_8);
        String expected =
                "HEADER1,HEADER2\n" +
                        "A1,B1\n" +
                        "A2,B2\n";

        assertEquals(expected, csv);
    }

    @Test
    void toBytes_withSpecialCharacters_quotesAndEscapesProperly() {
        List<List<String>> rows = List.of(
                List.of("a,b", "text \"with\" quotes", "line1\nline2")
        );

        CsvCreator creator = CsvCreator.builder("special.csv")
                .rows(rows)
                .lineSeparator("\n")
                .build();

        byte[] bytes = creator.toBytes();

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

        CsvCreator creator = CsvCreator.builder("semicolon.csv")
                .rows(rows)
                .delimiter(';')
                .lineSeparator("\n")
                .build();

        byte[] bytes = creator.toBytes();

        String csv = new String(bytes, StandardCharsets.UTF_8);
        String expected = "A;B;C\n";

        assertEquals(expected, csv);
    }

    @Test
    void toBytes_withNoLinesAndNoRows_createsEmptyContent() {
        CsvCreator creator = CsvCreator.builder("empty.csv")
                .lineSeparator("\n")
                .build();

        byte[] bytes = creator.toBytes();

        assertNotNull(bytes);
        String csv = new String(bytes, StandardCharsets.UTF_8);
        assertEquals("", csv);
    }

    @Test
    void toPath_writesFileOnDisk() throws IOException {
        List<List<String>> rows = List.of(
                List.of("C1", "D1")
        );

        CsvCreator creator = CsvCreator.builder("data.csv")
                .rows(rows)
                .lineSeparator("\n")
                .build();

        Path targetPath = tempDir.resolve("data.csv");

        Path resultPath = creator.toPath(targetPath);

        assertEquals(targetPath, resultPath);
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

        CsvCreator creator = CsvCreator.builder("file.csv")
                .rows(rows)
                .lineSeparator("\n")
                .build();

        Path targetPath = tempDir.resolve("file.csv");
        File targetFile = targetPath.toFile();

        File resultFile = creator.toFile(targetFile);

        assertEquals(targetFile, resultFile);
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

        CsvCreator creator = CsvCreator.builder("ignored.csv")
                .rows(rows)
                .lineSeparator("\n")
                .build();

        String fileName = "dir-file.csv";

        File resultFile = creator.toFile(tempDir, fileName);

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

        CsvCreator creator = CsvCreator.builder("temp.csv")
                .rows(rows)
                .lineSeparator("\n")
                .build();

        File tempFile = creator.toTempFile();

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

        CsvCreator creator = CsvCreator.builder("out.csv")
                .rows(rows)
                .lineSeparator("\n")
                .build();

        Path targetPath = tempDir.resolve("out.csv");

        try (OutputStream os = Files.newOutputStream(targetPath)) {
            creator.write(os);
        }

        assertTrue(Files.exists(targetPath));
        assertTrue(Files.size(targetPath) > 0L);

        String content = Files.readString(targetPath, StandardCharsets.UTF_8);
        assertEquals("O1,P1\n", content);
    }

    @Test
    void getFileNameAndProperties_returnValuesFromBuilder() {
        CsvCreator creator = CsvCreator.builder("named.csv")
                .delimiter(';')
                .lineSeparator("\n")
                .build();

        assertEquals("named.csv", creator.getFileName());
        assertEquals(';', creator.getDelimiter());
        assertEquals("\n", creator.getLineSeparator());
    }

    @Test
    void toBytes_usesUtf8Encoding() {
        List<List<String>> rows = List.of(
                List.of("한글", "テスト")
        );

        CsvCreator creator = CsvCreator.builder("utf8.csv")
                .rows(rows)
                .lineSeparator("\n")
                .build();

        byte[] bytes = creator.toBytes();

        String csv = new String(bytes, StandardCharsets.UTF_8);
        assertEquals("한글,テスト\n", csv);
    }

    @Test
    void generate_writesEmptyLineForEmptyRow() throws IOException {
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("A"));
        rows.add(List.of());
        rows.add(List.of("B"));

        CsvCreator creator = CsvCreator.builder("empty-row.csv")
                .rows(rows)
                .lineSeparator("\n")
                .build();

        byte[] bytes = creator.toBytes();
        String csv = new String(bytes, StandardCharsets.UTF_8);

        String expected =
                "A\n" +
                        "\n" +
                        "B\n";

        assertEquals(expected, csv);
    }

    @Test
    void toBytes_usesSystemLineSeparatorByDefault() throws IOException {
        List<List<String>> rows = List.of(
                List.of("A", "B"),
                List.of("C", "D")
        );

        CsvCreator creator = CsvCreator.builder("default-ls.csv")
                .rows(rows)
                .build();

        byte[] bytes = creator.toBytes();
        String csv = new String(bytes, StandardCharsets.UTF_8);

        String ls = System.lineSeparator();
        String expected =
                "A,B" + ls +
                        "C,D" + ls;

        assertEquals(expected, csv);
    }
}
