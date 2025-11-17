package com.shings.excelmaker;

import com.shings.excelmaker.exception.XlsxException;
import com.shings.excelmaker.xlsx.XlsxSheet;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.Entry;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XlsxCreatorTest {
    @TempDir
    Path tempDir;

    @Test
    void builder_nullFileName_throwsExceptionOnBuild() {
        XlsxCreator.Builder builder = XlsxCreator.builder(null);

        assertThrows(XlsxException.class, builder::build);
    }

    @Test
    void builder_blankFileName_throwsExceptionOnBuild() {
        XlsxCreator.Builder builder = XlsxCreator.builder("   ");

        assertThrows(XlsxException.class, builder::build);
    }

    @Test
    void builder_sheet_nullSheet_throwsException() {
        XlsxCreator.Builder builder = XlsxCreator.builder("test.xlsx");

        assertThrows(XlsxException.class, () -> builder.sheet(null));
    }

    @Test
    void builder_sheets_nullList_throwsException() {
        XlsxCreator.Builder builder = XlsxCreator.builder("test.xlsx");

        assertThrows(XlsxException.class, () -> builder.sheets(null));
    }

    @Test
    void builder_sheetLines_nullSheetName_throwsException() {
        XlsxCreator.Builder builder = XlsxCreator.builder("test.xlsx");
        List<String> lines = List.of("one", "two");

        assertThrows(XlsxException.class, () -> builder.sheetLines(null, lines));
    }

    @Test
    void builder_sheetLines_nullLines_throwsException() {
        XlsxCreator.Builder builder = XlsxCreator.builder("test.xlsx");

        assertThrows(XlsxException.class, () -> builder.sheetLines("Sheet1", null));
    }

    @Test
    void builder_sheetRows_nullSheetName_throwsException() {
        XlsxCreator.Builder builder = XlsxCreator.builder("test.xlsx");
        List<List<String>> rows = List.of(List.of("a", "b"));

        assertThrows(XlsxException.class, () -> builder.sheetRows(null, rows));
    }

    @Test
    void builder_sheetRows_nullRows_throwsException() {
        XlsxCreator.Builder builder = XlsxCreator.builder("test.xlsx");

        assertThrows(XlsxException.class, () -> builder.sheetRows("Sheet1", null));
    }

    @Test
    void write_nullOutputStream_throwsException() {
        XlsxCreator creator = XlsxCreator.builder("test.xlsx").build();

        assertThrows(XlsxException.class, () -> creator.write(null));
    }

    @Test
    void toPath_nullTargetPath_throwsException() {
        XlsxCreator creator = XlsxCreator.builder("test.xlsx").build();

        assertThrows(XlsxException.class, () -> creator.toPath(null));
    }

    @Test
    void toFile_nullTargetFile_throwsException() {
        XlsxCreator creator = XlsxCreator.builder("test.xlsx").build();

        assertThrows(XlsxException.class, () -> creator.toFile(null));
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

        XlsxCreator creator = XlsxCreator.builder("test.xlsx")
                .sheet(sheet)
                .build();

        byte[] bytes = creator.toBytes();

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

        XlsxCreator creator = XlsxCreator.builder("data.xlsx")
                .sheet(sheet)
                .build();

        Path targetPath = tempDir.resolve("data.xlsx");

        Path resultPath = creator.toPath(targetPath);

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

        XlsxCreator creator = XlsxCreator.builder("file.xlsx")
                .sheet(sheet)
                .build();

        Path targetPath = tempDir.resolve("file.xlsx");
        java.io.File targetFile = targetPath.toFile();

        java.io.File resultFile = creator.toFile(targetFile);

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

        XlsxCreator creator = XlsxCreator.builder("secret.xlsx")
                .sheet(sheet)
                .password(password)
                .build();

        byte[] encryptedBytes = creator.toBytes();

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

        XlsxCreator creator = XlsxCreator.builder("protected.xlsx")
                .sheet(sheet)
                .password("correct-password")  // 분명 패스워드 설정
                .build();

        byte[] bytes = creator.toBytes();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        // 1) 평문 XLSX로 열어보기
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            System.out.println(">>> Opened as plain XSSFWorkbook. -> NOT encrypted.");
            System.out.println("Sheets: " + workbook.getNumberOfSheets());
        } catch (Exception e) {
            System.out.println(">>> Cannot open as plain XSSFWorkbook. Maybe encrypted: " + e.getMessage());
        }

        // 2) POIFSFileSystem으로 열어보기
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

        XlsxCreator creator = XlsxCreator.builder("protected.xlsx")
                .sheet(sheet)
                .password("correct-password")
                .build();

        byte[] encryptedBytes = creator.toBytes();

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
        XlsxCreator creator = XlsxCreator.builder("empty.xlsx")
                .build();

        byte[] bytes = creator.toBytes();

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

        XlsxCreator creator = XlsxCreator.builder("out.xlsx")
                .sheet(sheet)
                .build();

        Path targetPath = tempDir.resolve("out.xlsx");

        try (OutputStream os = Files.newOutputStream(targetPath)) {
            creator.write(os);
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
}
