package com.example.demo.exceptions;

import com.example.demo.dto.InformationResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Request rejected: {} constraint violations", ex.getConstraintViolations().size());
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                errors.put(violation.getPropertyPath().toString(), violation.getMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(InvalidUserSearchException.class)
    public ResponseEntity<InformationResponse> handleInvalidUserSearch(InvalidUserSearchException ex) {
        log.warn("Request rejected: invalid user search");
        return ResponseEntity.badRequest().body(new InformationResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException(MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Request rejected: {} validation errors", ex.getBindingResult().getErrorCount());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(InvalidAgeException.class)
    public ResponseEntity<InformationResponse> handleHttpMessageNotReadable(InvalidAgeException ex) {
        log.warn("Request rejected: minimum age requirement not met");
        InformationResponse errorObject = new InformationResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorObject);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<InformationResponse> handleUserNotFoundException(UserNotFoundException ex) {
        log.warn("Request rejected: user not found");
        InformationResponse errorObject = new InformationResponse(ex.getMessage());
        return new ResponseEntity<>(errorObject, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<InformationResponse> handleDateTimeParseException(DateTimeParseException ex) {
        log.warn("Request rejected: invalid date format");
        InformationResponse errorObject = new InformationResponse(ex.getMessage());
        return new ResponseEntity<>(errorObject, HttpStatus.BAD_REQUEST);
    }
}
