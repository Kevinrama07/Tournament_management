package com.example.tournament.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldViolation> fieldViolations
) {

    public record FieldViolation(String field, String message) {
    }
}