package com.mjc.school.exception;

public class ApiVersionNotSupportedException extends RuntimeException {
    public ApiVersionNotSupportedException(String message) {
        super(message);
    }
}
