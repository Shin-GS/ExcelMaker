package com.shings.excelmaker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractMaker<E extends RuntimeException> {
    private final String fileName;

    protected AbstractMaker(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw createException("fileName must not be null or blank.", null);
        }

        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public final byte[] toBytes() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            generate(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw createException("Failed to convert content to byte array.", e);
        }
    }

    public final void write(OutputStream out) {
        if (out == null) {
            throw createException("OutputStream must not be null.", null);
        }

        try {
            generate(out);

        } catch (IOException e) {
            throw createException("Failed to write content to OutputStream.", e);
        }
    }

    public final void toPath(Path targetPath) {
        if (targetPath == null) {
            throw createException("targetPath must not be null.", null);
        }

        try (OutputStream outputStream = Files.newOutputStream(targetPath)) {
            generate(outputStream);

        } catch (IOException e) {
            throw createException("Failed to write content to file: " + targetPath, e);
        }
    }

    public final void toFile(File targetFile) {
        if (targetFile == null) {
            throw createException("targetFile must not be null.", null);
        }

        toPath(targetFile.toPath());
    }

    public final File toFile(Path dir, String fileName) {
        if (dir == null) {
            throw createException("dir must not be null.", null);
        }

        if (fileName == null || fileName.isBlank()) {
            throw createException("fileName must not be null or blank.", null);
        }

        Path resolved = dir.resolve(fileName);
        toPath(resolved);
        return resolved.toFile();
    }

    public final File toTempFile(String suffix) {
        if (suffix == null || suffix.isBlank()) {
            throw createException("suffix must not be null or blank.", null);
        }

        try {
            Path temp = Files.createTempFile(null, suffix);
            try (OutputStream out = Files.newOutputStream(temp)) {
                generate(out);
            }

            return temp.toFile();

        } catch (IOException e) {

            throw createException("Failed to generate temporary file.", e);
        }
    }

    protected abstract void generate(OutputStream out) throws IOException;

    protected abstract E createException(String message, Throwable cause);
}
