package com.shings.excelmaker;

import com.shings.excelmaker.exception.XlsxException;
import com.shings.excelmaker.xlsx.XlsxSheet;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XlsxMakerTest {
    @TempDir
    Path tempDir;

    @Test
    void builder_nullFileName_throwsExceptionOnBuild() {
        XlsxMaker.Builder builder = XlsxMaker.builder(null);

        assertThrows(XlsxException.class, builder::build);
    }

    @Test
    void builder_blankFileName_throwsExceptionOnBuild() {
        XlsxMaker.Builder builder = XlsxMaker.builder("   ");

        assertThrows(XlsxException.class, builder::build);
    }

    @Test
    void builder_sheet_nullSheet_throwsException() {
        XlsxMaker.Builder builder = XlsxMaker.builder("test.xlsx");

        assertThrows(XlsxException.class, () -> builder.sheet(null));
    }

    @Test
    void builder_sheets_nullList_throwsException() {
        XlsxMaker.Builder builder = XlsxMaker.builder("test.xlsx");

        assertThrows(XlsxException.class, () -> builder.sheets(null));
    }

    @Test
    void builder_sheetLines_nullSheetName_throwsException() {
        XlsxMaker.Builder builder = XlsxMaker.builder("test.xlsx");
        List<String> lines = List.of("one", "two");

        assertThrows(XlsxException.class, () -> builder.sheetLines(null, lines));
    }

    @Test
    void builder_sheetLines_nullLines_throwsException() {
        XlsxMaker.Builder builder = XlsxMaker.builder("test.xlsx");

        assertThrows(XlsxException.class, () -> builder.sheetLines("Sheet1", null));
    }

    @Test
    void builder_sheetRows_nullSheetName_throwsException() {
        XlsxMaker.Builder builder = XlsxMaker.builder("test.xlsx");
        List<List<String>> rows = List.of(List.of("a", "b"));

        assertThrows(XlsxException.class, () -> builder.sheetRows(null, rows));
    }

    @Test
    void builder_sheetRows_nullRows_throwsException() {
        XlsxMaker.Builder builder = XlsxMaker.builder("test.xlsx");

        assertThrows(XlsxException.class, () -> builder.sheetRows("Sheet1", null));
    }

    @Test
    void write_nullOutputStream_throwsException() {
        XlsxMaker maker = XlsxMaker.builder("test.xlsx").build();

        assertThrows(XlsxException.class, () -> maker.write(null));
    }

    @Test
    void toPath_nullTargetPath_throwsException() {
        XlsxMaker maker = XlsxMaker.builder("test.xlsx").build();

        assertThrows(XlsxException.class, () -> maker.toPath(null));
    }

    @Test
    void toFile_nullTargetFile_throwsException() {
        XlsxMaker maker = XlsxMaker.builder("test.xlsx").build();

        assertThrows(XlsxException.class, () -> maker.toFile((java.io.File) null));
    }

    @Test
    void toBytes_createsWorkbookWithSheetAndRows() {
        List<List<String>> rows = List.of(
                List.of("A1", "B1"),
                List.of("A2", "B2")
        );

        XlsxSheet sheet = XlsxSheet.builder("Sheet1")
                .rows(rows)
                .build();

        XlsxMaker maker = XlsxMaker.builder("test.xlsx")
                .sheet(sheet)
                .build();

        byte[] bytes = maker.toBytes();

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(1, workbook.getNumberOfSheets());

            Sheet poiSheet = workbook.getSheet("Sheet1");
            assertNotNull(poiSheet);

            Row row0 = poiSheet.getRow(0);
            assertNotNull(row0);
            assertEquals("A1", row0.getCell(0).getStringCellValue());
            assertEquals("B1", row0.getCell(1).getStringCellValue());

            Row row1 = poiSheet.getRow(1);
            assertNotNull(row1);
            assertEquals("A2", row1.getCell(0).getStringCellValue());
            assertEquals("B2", row1.getCell(1).getStringCellValue());

        } catch (IOException e) {
            fail("Should be able to read generated workbook", e);
        }
    }

    @Test
    void toPath_writesFileOnDisk() throws IOException {
        List<List<String>> rows = List.of(
                List.of("C1", "D1")
        );

        XlsxSheet sheet = XlsxSheet.builder("Data")
                .rows(rows)
                .build();

        XlsxMaker maker = XlsxMaker.builder("data.xlsx")
                .sheet(sheet)
                .build();

        Path targetPath = tempDir.resolve("data.xlsx");

        Path resultPath = maker.toPath(targetPath);

        assertEquals(targetPath, resultPath);
        assertTrue(Files.exists(targetPath));
        assertTrue(Files.size(targetPath) > 0L);

        try (XSSFWorkbook workbook =
                     new XSSFWorkbook(Files.newInputStream(targetPath))) {
            assertEquals(1, workbook.getNumberOfSheets());
            Sheet poiSheet = workbook.getSheet("Data");
            assertNotNull(poiSheet);

        } catch (IOException e) {
            fail("Should be able to read saved workbook from disk", e);
        }
    }

    @Test
    void toFile_writesFileOnDisk() {
        List<List<String>> rows = List.of(
                List.of("X1", "Y1")
        );

        XlsxSheet sheet = XlsxSheet.builder("SheetX")
                .rows(rows)
                .build();

        XlsxMaker maker = XlsxMaker.builder("file.xlsx")
                .sheet(sheet)
                .build();

        Path targetPath = tempDir.resolve("file.xlsx");
        java.io.File targetFile = targetPath.toFile();

        java.io.File resultFile = maker.toFile(targetFile);

        assertEquals(targetFile, resultFile);
        assertTrue(targetFile.exists());
        assertTrue(targetFile.length() > 0L);
    }

    @Test
    void toBytes_withPassword_createsEncryptedWorkbook() {
        List<List<String>> rows = List.of(
                List.of("E1", "F1")
        );

        XlsxSheet sheet = XlsxSheet.builder("SecretSheet")
                .rows(rows)
                .build();

        String password = "s3cr3t";

        XlsxMaker maker = XlsxMaker.builder("secret.xlsx")
                .sheet(sheet)
                .password(password)
                .build();

        byte[] encryptedBytes = maker.toBytes();

        assertNotNull(encryptedBytes);
        assertTrue(encryptedBytes.length > 0);

        try (ByteArrayInputStream bis = new ByteArrayInputStream(encryptedBytes);
             POIFSFileSystem fs = new POIFSFileSystem(bis)) {

            EncryptionInfo info = new EncryptionInfo(fs);
            Decryptor decryptor = Decryptor.getInstance(info);

            assertTrue(decryptor.verifyPassword(password),
                    "Password should decrypt the workbook");

            try (OPCPackage opc = OPCPackage.open(decryptor.getDataStream(fs));
                 XSSFWorkbook workbook = new XSSFWorkbook(opc)) {

                assertEquals(1, workbook.getNumberOfSheets());

                Sheet poiSheet = workbook.getSheet("SecretSheet");
                assertNotNull(poiSheet);

                Row row0 = poiSheet.getRow(0);
                assertNotNull(row0);
                assertEquals("E1", row0.getCell(0).getStringCellValue());
                assertEquals("F1", row0.getCell(1).getStringCellValue());
            }

        } catch (Exception e) {
            fail("Should be able to decrypt and open password-protected workbook", e);
        }
    }

    @Test
    void debug_checkIfEncryptedOrPlain() {
        List<List<String>> rows = List.of(
                List.of("A1", "B1")
        );

        XlsxSheet sheet = XlsxSheet.builder("Protected")
                .rows(rows)
                .build();

        XlsxMaker maker = XlsxMaker.builder("protected.xlsx")
                .sheet(sheet)
                .password("correct-password")
                .build();

        byte[] bytes = maker.toBytes();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            System.out.println(">>> Opened as plain XSSFWorkbook. -> NOT encrypted.");
            System.out.println("Sheets: " + workbook.getNumberOfSheets());
        } catch (Exception e) {
            System.out.println(">>> Cannot open as plain XSSFWorkbook. Maybe encrypted: " + e.getMessage());
        }

        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             POIFSFileSystem fs = new POIFSFileSystem(bis)) {

            System.out.println(">>> POIFS root entries: " + fs.getRoot().getEntryNames());
        } catch (Exception e) {
            System.out.println(">>> Cannot open as POIFSFileSystem: " + e.getMessage());
        }
    }

    @Test
    void toBytes_withPassword_wrongPasswordCannotDecrypt() {
        List<List<String>> rows = List.of(
                List.of("Z1", "Z2")
        );

        XlsxSheet sheet = XlsxSheet.builder("Protected")
                .rows(rows)
                .build();

        XlsxMaker maker = XlsxMaker.builder("protected.xlsx")
                .sheet(sheet)
                .password("correct-password")
                .build();

        byte[] encryptedBytes = maker.toBytes();

        assertNotNull(encryptedBytes);
        assertTrue(encryptedBytes.length > 0);

        try (ByteArrayInputStream bis = new ByteArrayInputStream(encryptedBytes);
             POIFSFileSystem fs = new POIFSFileSystem(bis)) {

            EncryptionInfo info = new EncryptionInfo(fs);
            Decryptor decryptor = Decryptor.getInstance(info);

            assertFalse(decryptor.verifyPassword("wrong-password"),
                    "Wrong password must not decrypt the workbook");
        } catch (Exception e) {
            fail("Unexpected exception while checking wrong password behavior", e);
        }
    }

    @Test
    void toBytes_withNoSheets_createsEmptyWorkbook() {
        XlsxMaker maker = XlsxMaker.builder("empty.xlsx")
                .build();

        byte[] bytes = maker.toBytes();

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(0, workbook.getNumberOfSheets());
        } catch (IOException e) {
            fail("Should be able to read empty workbook", e);
        }
    }

    @Test
    void write_writesToProvidedOutputStream() throws IOException {
        List<List<String>> rows = List.of(
                List.of("O1", "P1")
        );

        XlsxSheet sheet = XlsxSheet.builder("OutSheet")
                .rows(rows)
                .build();

        XlsxMaker maker = XlsxMaker.builder("out.xlsx")
                .sheet(sheet)
                .build();

        Path targetPath = tempDir.resolve("out.xlsx");

        try (OutputStream os = Files.newOutputStream(targetPath)) {
            maker.write(os);
        }

        assertTrue(Files.exists(targetPath));
        assertTrue(Files.size(targetPath) > 0L);

        try (XSSFWorkbook workbook =
                     new XSSFWorkbook(Files.newInputStream(targetPath))) {
            assertEquals(1, workbook.getNumberOfSheets());
            assertNotNull(workbook.getSheet("OutSheet"));
        } catch (IOException e) {
            fail("Should be able to read workbook written via write(OutputStream)", e);
        }
    }

    // ----------------------------------------------------------------------
    // Additional tests
    // ----------------------------------------------------------------------

    @Test
    void builder_sheets_originalListMutationDoesNotAffectmaker() {
        List<List<String>> rows = List.of(List.of("A1"));
        XlsxSheet sheet1 = XlsxSheet.builder("S1")
                .rows(rows)
                .build();
        XlsxSheet sheet2 = XlsxSheet.builder("S2")
                .rows(rows)
                .build();

        List<XlsxSheet> originalList = new ArrayList<>();
        originalList.add(sheet1);

        XlsxMaker.Builder builder = XlsxMaker.builder("test.xlsx")
                .sheets(originalList);

        originalList.add(sheet2);

        XlsxMaker maker = builder.build();

        assertEquals(1, maker.getSheets().size());
        assertEquals("S1", maker.getSheets().get(0).getSheetName());
    }

    @Test
    void builder_password_setsPasswordOnmaker() {
        XlsxMaker maker = XlsxMaker.builder("pwd.xlsx")
                .password("password123")
                .build();

        assertEquals("password123", maker.getPassword());
    }

    @Test
    void getFileNameAndSheets_returnValuesFromBuilder() {
        List<List<String>> rows = List.of(List.of("A1"));
        XlsxSheet sheet = XlsxSheet.builder("Sheet1")
                .rows(rows)
                .build();

        XlsxMaker maker = XlsxMaker.builder("named.xlsx")
                .sheet(sheet)
                .build();

        assertEquals("named.xlsx", maker.getFileName());
        assertEquals(1, maker.getSheets().size());
        assertEquals("Sheet1", maker.getSheets().get(0).getSheetName());
    }

    @Test
    void toFile_withDirAndFileName_writesFileOnDisk() throws IOException {
        List<List<String>> rows = List.of(
                List.of("D1", "E1")
        );

        XlsxSheet sheet = XlsxSheet.builder("DirSheet")
                .rows(rows)
                .build();

        XlsxMaker maker = XlsxMaker.builder("ignored.xlsx")
                .sheet(sheet)
                .build();

        String fileName = "dir-file.xlsx";

        java.io.File resultFile = maker.toFile(tempDir, fileName);

        assertNotNull(resultFile);
        assertTrue(resultFile.exists());
        assertTrue(resultFile.length() > 0L);
        assertEquals(tempDir.resolve(fileName), resultFile.toPath());

        try (XSSFWorkbook workbook =
                     new XSSFWorkbook(Files.newInputStream(resultFile.toPath()))) {
            assertEquals(1, workbook.getNumberOfSheets());
            assertNotNull(workbook.getSheet("DirSheet"));
        } catch (IOException e) {
            fail("Should be able to read workbook written via toFile(Path, String)", e);
        }
    }

    @Test
    void toFile_withDirNull_throwsException() {
        XlsxMaker maker = XlsxMaker.builder("test.xlsx").build();

        assertThrows(XlsxException.class, () -> maker.toFile(null, "file.xlsx"));
    }

    @Test
    void toFile_withBlankFileName_throwsException() {
        XlsxMaker maker = XlsxMaker.builder("test.xlsx").build();

        assertThrows(XlsxException.class, () -> maker.toFile(tempDir, "   "));
    }

    @Test
    void toTempFile_createsPhysicalTempFile() {
        List<List<String>> rows = List.of(
                List.of("T1", "T2")
        );

        XlsxSheet sheet = XlsxSheet.builder("TempSheet")
                .rows(rows)
                .build();

        XlsxMaker maker = XlsxMaker.builder("temp.xlsx")
                .sheet(sheet)
                .build();

        java.io.File tempFile = maker.toTempFile();

        assertNotNull(tempFile);
        assertTrue(tempFile.exists());
        assertTrue(tempFile.length() > 0L);

        try (XSSFWorkbook workbook =
                     new XSSFWorkbook(Files.newInputStream(tempFile.toPath()))) {
            assertEquals(1, workbook.getNumberOfSheets());
            assertNotNull(workbook.getSheet("TempSheet"));
        } catch (IOException e) {
            fail("Should be able to read workbook from temporary file", e);
        }
    }
}
