# excelmaker

Simple Java library to generate CSV and XLSX files with a fluent builder API.

## Features

- Generate CSV files from lists of rows or lines
- Generate XLSX files with multiple sheets
- Support for header styling (font color, background color, alignment, border)
- Password-protected XLSX (Agile encryption via Apache POI)
- Unified output API:
  - `toBytes()`
  - `write(OutputStream out)`
  - `toPath(Path targetPath)`
  - `toFile(File targetFile)`
  - `toFile(Path dir, String fileName)`
  - `toTempFile(String suffix)` or convenience methods

## Installation

**Maven**

```xml
<dependency>
    <groupId>com.shings</groupId>
    <artifactId>excelmaker</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle**

```groovy
implementation "com.shings:excelmaker:1.0.0"
```

## Quick Start

### CSV

```java
import com.shings.excelmaker.CsvMaker;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

public class CsvExample {

    public static void main(String[] args) {
        CsvMaker csvMaker = CsvMaker.builder("users.csv")
                .row(Arrays.asList("id", "name"))
                .row(Arrays.asList("1", "Alice"))
                .row(Arrays.asList("2", "Bob"))
                .build();

        byte[] bytes = csvMaker.toBytes();

        File file = csvMaker.toFile(
                Paths.get("output"),
                csvMaker.getFileName()
        );

        File tempFile = csvMaker.toTempFile();
    }
}
```

### XLSX

```java
import com.shings.excelmaker.XlsxMaker;
import com.shings.excelmaker.xlsx.XlsxSheet;

import java.io.File;
import java.util.Arrays;

public class XlsxExample {

    public static void main(String[] args) {
        XlsxSheet sheet = XlsxSheet.builder("users")
                .header(Arrays.asList("id", "name"))
                .rows(Arrays.asList(
                        Arrays.asList("1", "Alice"),
                        Arrays.asList("2", "Bob")
                ))
                .build();

        XlsxMaker maker = XlsxMaker.builder("users.xlsx")
                .sheet(sheet)
                .build();

        File tempFile = maker.toTempFile();

        byte[] bytes = maker.toBytes();
    }
}
```

## Documentation

- [v1 Documentation](./docs/v1/README.md)
