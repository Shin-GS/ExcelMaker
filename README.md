# excelmaker

![Java](https://img.shields.io/badge/Java-17%2B-blue.svg)
![Build](https://img.shields.io/badge/Build-Ready-success.svg)
![License](https://img.shields.io/badge/License-Custom-lightgrey.svg)

A fast, safe, and extensible Java library for generating **CSV** and **XLSX** files through a clean and fluent builder
API.

> This library was created to eliminate repetitive Excel/CSV export code across different projects  
> and provide a more extensible, reliable, and high-performance solution.

---

## ✨ Features

- **Fluent API** for CSV/XLSX generation
- **CSV** generation from lines or row-based structures
- **XLSX** generation with:
    - Multiple sheets
    - Styled headers (font color, background color, alignment, borders, column width)
    - Optional password protection (Apache POI Agile Encryption)
- **Unified output API**
    - `toBytes()`
    - `write(OutputStream out)`
    - `toPath(Path targetPath)`
    - `toFile(File targetFile)`
    - `toFile(Path dir, String fileName)`
    - `toTempFile(String suffix)`
- **Defensive copy** strategy for all collections
- **Dedicated exception hierarchy** (CsvException / XlsxException)

---

## 📦 Installation

**Maven**

```xml
<dependency>
    <groupId>io.github.shin-gs</groupId>
    <artifactId>excelmaker</artifactId>
    <version>0.0.1</version>
</dependency>
```

**Gradle (Groovy DSL)**

```groovy
implementation "io.github.shin-gs:excelmaker:0.0.1"
```

**Gradle (Kotlin DSL)**

```groovy
implementation("io.github.shin-gs:excelmaker:0.0.1")
```

---

## 🚀 Quick Start

### CSV Example

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

### XLSX Example

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

---

## 📚 Documentation

Full version-specific documentation can be found under:

- [v1 Documentation](./docs/v1/README.md)

---

## 🔄 Versioning Policy

- **Major Versions (e.g., 1.x → 2.x)**  
  API structure and usage may change.  
  Documentation is maintained separately under `/docs/v{version}`.

- **Minor Versions (e.g., 1.0 → 1.1)**  
  Fully backward compatible.  
  Existing API usage will remain supported.

---

## 💬 Feedback & Ideas

If you have suggestions or ideas to improve this library, feel free to contact:

**rudtjq1213@gmail.com**

Fast and detailed responses are guaranteed.

