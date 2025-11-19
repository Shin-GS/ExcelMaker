# CSV Documentation (v1.x)

`CsvMaker` is a concrete implementation of `AbstractMaker` that generates CSV content.

## 1. Creating a CsvMaker

```java
CsvMaker maker = CsvMaker.builder("users.csv")
        .row(Arrays.asList("id", "name"))
        .row(Arrays.asList("1", "Alice"))
        .row(Arrays.asList("2", "Bob"))
        .build();
```

## 2. Adding Data

Supports:
- `line(String)`
- `lines(List<String>)`
- `row(List<String>)`
- `rows(List<List<String>>)`.

## 3. Delimiter & Line Separator

Customizable via:
- `delimiter(char)`
- `lineSeparator(String)`.

## 4. CSV Encoding Rules

Automatic escaping for:
- delimiter
- quotes
- newlines.

## 5. Output Methods

Inherited from `AbstractMaker`:
- `toBytes()`
- `write(OutputStream)`
- `toPath(Path)`
- `toFile(File)`
- `toFile(Path,String)`
- `toTempFile()`.
