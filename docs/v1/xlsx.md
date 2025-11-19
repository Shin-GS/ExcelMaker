# XLSX Documentation (v1.x)

`XlsxMaker` builds XLSX files using Apache POI SXSSFWorkbook.

## 1. Basic XLSX

```java
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
```

## 2. XlsxSheet

Supports:
- header(List<String>)
- headerStyled(List<XlsxSheetCell>)
- rows(...)
- lines(...)

## 3. XlsxSheetCell

Configurable:
- width
- backgroundColor
- fontColor
- alignment
- border.

## 4. Password Protection

`password(String)` enables Agile-encrypted XLSX output.

## 5. Output Methods

Same as `AbstractMaker`.
