package com.example.technicaltask.exception;

public class JobParsingException extends RuntimeException {
    public JobParsingException(String message) {
        super(message);
    }

    public JobParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}