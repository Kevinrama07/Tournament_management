package com.example.tournament.exception;

public class InvalidOperationException extends BusinessException {

    private static final long serialVersionUID = 1L;

	public InvalidOperationException(String message) {
        super(message);
    }
}