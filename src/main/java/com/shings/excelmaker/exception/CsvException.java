package com.shings.excelmaker.exception;

/**
 * Custom exception for CSV generation errors.
 * This exception wraps underlying I/O or processing exceptions
 * to provide a unified runtime error for CsvMaker operations.
 */
public class CsvException extends RuntimeException {

    /**
     * Constructor with only message.
     *
     * @param message the error message.
     */
    public CsvException(String message) {
        super(message);
    }

    /**
     * Constructor with message and underlying cause.
     *
     * @param message the error message.
     * @param cause   the original exception.
     */
    public CsvException(String message, Throwable cause) {
        super(message, cause);
    }
}
