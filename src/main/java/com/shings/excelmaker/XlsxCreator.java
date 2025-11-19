package com.shings.excelmaker;

import com.shings.excelmaker.exception.XlsxException;
import com.shings.excelmaker.xlsx.XlsxSheet;
import com.shings.excelmaker.xlsx.XlsxSheetCell;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public final class XlsxCreator {
    private final String fileName;
    private final List<XlsxSheet> sheets;
    private final String password;

    private XlsxCreator(Builder builder) {
        if (builder.fileName == null || builder.fileName.isBlank()) {
            throw new XlsxException("fileName must not be null or blank.");
        }

        this.fileName = builder.fileName;
        this.sheets = List.copyOf(builder.sheets);
        this.password = builder.password;
    }

    public static Builder builder(String fileName) {
        return new Builder(fileName);
    }

    public String getFileName() {
        return fileName;
    }

    public List<XlsxSheet> getSheets() {
        return sheets;
    }

    public String getPassword() {
        return password;
    }

    public byte[] toBytes() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            generate(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new XlsxException("Failed to convert XLSX workbook to byte array.");
        }
    }

    public void write(OutputStream out) {
        if (out == null) {
            throw new XlsxException("OutputStream must not be null.");
        }

        generate(out);
    }

    public Path toPath(Path targetPath) {
        if (targetPath == null) {
            throw new XlsxException("targetPath must not be null.");
        }

        try (OutputStream outputStream = Files.newOutputStream(targetPath)) {
            generate(outputStream);
            return targetPath;

        } catch (IOException e) {
            throw new XlsxException("Failed to write XLSX file: " + targetPath, e);
        }
    }

    public File toFile(File targetFile) {
        if (targetFile == null) {
            throw new XlsxException("targetFile must not be null.");
        }

        toPath(targetFile.toPath());
        return targetFile;
    }

    private void generate(OutputStream out) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            workbook.setCompressTempFiles(true);
            fillWorkbook(workbook);

            if (password == null || password.isBlank()) {
                writePlainWorkbook(workbook, out);
                return;
            }

            writeEncryptedWorkbook(workbook, out, password);

        } catch (IOException e) {
            throw new XlsxException("Failed to generate XLSX workbook.", e);
        }
    }

    private void writePlainWorkbook(SXSSFWorkbook workbook, OutputStream out) throws IOException {
        workbook.write(out);
    }

    private void writeEncryptedWorkbook(SXSSFWorkbook workbook,
                                        OutputStream out,
                                        String password) throws IOException {
        try (POIFSFileSystem fs = new POIFSFileSystem()) {
            EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
            Encryptor encryptor = info.getEncryptor();
            encryptor.confirmPassword(password);

            try (OutputStream encryptorDataStream = encryptor.getDataStream(fs)) {
                workbook.write(encryptorDataStream);

            } catch (GeneralSecurityException e) {
                throw new XlsxException("Failed to encrypt XLSX workbook.", e);
            }

            fs.writeFilesystem(out);
        }
    }

    private void fillWorkbook(SXSSFWorkbook workbook) {
        for (XlsxSheet sheetSpec : sheets) {
            Sheet sheet = workbook.createSheet(sheetSpec.getSheetName());
            if (!sheetSpec.hasHeader()) {
                renderBody(sheet, sheetSpec.getRows(), false);
                continue;
            }

            renderHeader(workbook, sheet, sheetSpec.getHeaderCells());
            renderBody(sheet, sheetSpec.getRows(), true);
        }
    }

    private void renderHeader(SXSSFWorkbook workbook,
                              Sheet sheet,
                              List<XlsxSheetCell> headerCells) {
        Row headerRow = sheet.createRow(0);
        for (int columnIndex = 0; columnIndex < headerCells.size(); columnIndex++) {
            XlsxSheetCell headerCellSpec = headerCells.get(columnIndex);

            Cell cell = headerRow.createCell(columnIndex);
            cell.setCellValue(headerCellSpec.getText());

            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);

            if (headerCellSpec.getFontColor() != null) {
                font.setColor(headerCellSpec.getFontColor().toPoiColorIndex());
            }
            style.setFont(font);

            if (headerCellSpec.getBackgroundColor() != null) {
                style.setFillForegroundColor(headerCellSpec.getBackgroundColor().toPoiColorIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }

            if (headerCellSpec.getHorizontalAlignment() != null) {
                style.setAlignment(headerCellSpec.getHorizontalAlignment().toPoiAlignment());
            }

            if (headerCellSpec.getBorder() != null) {
                BorderStyle border = headerCellSpec.getBorder().toPoiBorder();
                style.setBorderTop(border);
                style.setBorderBottom(border);
                style.setBorderLeft(border);
                style.setBorderRight(border);
            }

            cell.setCellStyle(style);

            if (headerCellSpec.getColumnWidth() != null) {
                sheet.setColumnWidth(columnIndex, headerCellSpec.getColumnWidth());
            }
        }
    }

    private void renderBody(Sheet sheet,
                            List<List<String>> rows,
                            boolean hasHeader) {
        if (rows == null || rows.isEmpty()) {
            return;
        }

        int rowIndex = hasHeader ? 1 : 0;
        for (List<String> row : rows) {
            if (row == null || row.isEmpty()) {
                rowIndex++;
                continue;
            }

            Row sheetRow = sheet.createRow(rowIndex++);
            for (int columnIndex = 0; columnIndex < row.size(); columnIndex++) {
                String value = row.get(columnIndex);
                if (value != null) {
                    Cell cell = sheetRow.createCell(columnIndex);
                    cell.setCellValue(value);
                }
            }
        }
    }

    public static final class Builder {
        private final String fileName;
        private final List<XlsxSheet> sheets = new ArrayList<>();
        private String password;

        public Builder(String fileName) {
            this.fileName = fileName;
        }

        public Builder sheet(XlsxSheet sheet) {
            if (sheet == null) {
                throw new XlsxException("sheet must not be null.");
            }

            sheets.add(sheet);
            return this;
        }

        public Builder sheets(List<XlsxSheet> sheetList) {
            if (sheetList == null) {
                throw new XlsxException("sheetList must not be null.");
            }

            if (!sheetList.isEmpty()) {
                sheets.addAll(List.copyOf(sheetList));
            }

            return this;
        }

        public Builder sheetLines(String sheetName, List<String> lines) {
            if (sheetName == null) {
                throw new XlsxException("sheetName must not be null.");
            }

            if (lines == null) {
                throw new XlsxException("lines must not be null.");
            }

            XlsxSheet sheet = XlsxSheet.builder(sheetName)
                    .lines(lines)
                    .build();
            return sheet(sheet);
        }

        public Builder sheetRows(String sheetName, List<List<String>> rows) {
            if (sheetName == null) {
                throw new XlsxException("sheetName must not be null.");
            }

            if (rows == null) {
                throw new XlsxException("rows must not be null.");
            }

            XlsxSheet sheet = XlsxSheet.builder(sheetName)
                    .rows(rows)
                    .build();
            return sheet(sheet);
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public XlsxCreator build() {
            return new XlsxCreator(this);
        }
    }
}
