package com.example.tournament.exception;

public abstract class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	protected BusinessException(String message) {
        super(message);
    }

    protected BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}