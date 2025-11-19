package com.shings.excelmaker.exception;

/**
 * Custom exception for XLSX generation errors.
 * This exception encapsulates underlying I/O or processing exceptions
 * to provide a unified runtime error during Xlsxmaker operations.
 */
public class XlsxException extends RuntimeException {

    /**
     * Constructor with only message.
     *
     * @param message the error message.
     */
    public XlsxException(String message) {
        super(message);
    }

    /**
     * Constructor with message and underlying cause.
     *
     * @param message the error message describing the issue.
     * @param cause   the original exception that triggered this error.
     */
    public XlsxException(String message, Throwable cause) {
        super(message, cause);
    }
}
