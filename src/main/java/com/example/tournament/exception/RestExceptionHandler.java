package com.example.tournament.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex,
                                                    HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex,
                                                    HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ApiError> handleInvalidOperation(InvalidOperationException ex,
                                                            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                      HttpServletRequest request) {
        List<ApiError.FieldViolation> violations = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            violations.add(new ApiError.FieldViolation(error.getField(), error.getDefaultMessage()));
        }
        return build(HttpStatus.BAD_REQUEST, "Données invalides : veuillez corriger les champs signalés.",
                request, violations);
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(jakarta.validation.ConstraintViolationException ex,
                                                               HttpServletRequest request) {
        List<ApiError.FieldViolation> violations = new ArrayList<>();
        ex.getConstraintViolations().forEach(cv ->
                violations.add(new ApiError.FieldViolation(
                        cv.getPropertyPath().toString(), cv.getMessage())));
        return build(HttpStatus.BAD_REQUEST, "Paramètres invalides.", request, violations);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex,
                                                      HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "Corps de requête illisible ou mal formé.", request, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                        HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "Valeur de paramètre invalide : '" + ex.getValue() + "' pour "
                        + ex.getName() + ".", request, List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                              HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex,
                                                      HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Ressource introuvable : " + request.getRequestURI(),
                request, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex,
                                                         HttpServletRequest request) {
        return build(HttpStatus.CONFLICT,
                "Conflit de données : cette opération viole une contrainte existante.",
                request, List.of());
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            org.springframework.security.access.AccessDeniedException ex,
            HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Accès refusé : droits insuffisants.", request, List.of());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex,
                                                    HttpServletRequest request) {
        HttpStatus status = (ex instanceof ResourceNotFoundException)
                ? HttpStatus.NOT_FOUND
                : (ex instanceof ConflictException ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST);
        return build(status, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erreur interne du serveur.", request, List.of());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message,
                                            HttpServletRequest request,
                                            List<ApiError.FieldViolation> violations) {
        ApiError body = new ApiError(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                message, request.getRequestURI(), violations);
        return ResponseEntity.status(status).body(body);
    }
}