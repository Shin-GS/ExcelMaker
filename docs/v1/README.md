# excelmaker v1.x Documentation

This document describes version 1.x of `excelmaker`.

excelmaker provides a simple, fluent Java API to generate CSV and XLSX files.  
It exposes a common abstraction for output targets (bytes, stream, file, temp file), so you can focus on data,
not I/O boilerplate.

## 1. Core Abstraction

At the heart of the library is the `AbstractMaker<E extends RuntimeException>` class.

Key responsibilities:

- Validate basic parameters such as file name and paths
- Provide common output methods:
  - `toBytes()`
  - `write(OutputStream out)`
  - `toPath(Path targetPath)`
  - `toFile(File targetFile)`
  - `toFile(Path dir, String fileName)`
  - `toTempFile(String suffix)`
- Delegate actual content generation to `generate(OutputStream out)`
- Wrap `IOException` and validation failures into a specific runtime exception type

Concrete makers implement:

- `protected abstract void generate(OutputStream out) throws IOException;`
- `protected abstract E createException(String message, Throwable cause);`

## 2. CSV Support

See [CSV Documentation](csv.md) for detailed examples.

## 3. XLSX Support

See [XLSX Documentation](xlsx.md) for detailed examples.

## 4. Advanced Usage

See [Advanced Usage](advanced.md).

## 5. Versioning

This document targets excelmaker v1.x.
