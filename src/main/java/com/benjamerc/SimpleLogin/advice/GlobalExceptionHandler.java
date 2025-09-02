package com.benjamerc.SimpleLogin.advice;

import com.benjamerc.SimpleLogin.exception.EmailAlreadyExistsException;
import com.benjamerc.SimpleLogin.exception.InvalidCredentialsException;
import com.benjamerc.SimpleLogin.exception.UserNotFoundByEmailException;
import com.benjamerc.SimpleLogin.exception.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected error: " + e.getMessage(),
                List.of(e.getMessage()),
                e.getClass().getSimpleName(),
                request.getRequestURI());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFound(UsernameNotFoundException e, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND,
                "User not found",
                List.of(e.getMessage()),
                e.getClass().getSimpleName(),
                request.getRequestURI());
    }

    @ExceptionHandler(UserNotFoundByEmailException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundByEmail(UserNotFoundByEmailException e, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND,
                "User not found with that email address",
                List.of("No user exists with the provided email"),
                e.getClass().getSimpleName(),
                request.getRequestURI());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException e, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED,
                "Invalid email or password",
                List.of("Please check your credentials and try again"),
                e.getClass().getSimpleName(),
                request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();

        return buildErrorResponse(HttpStatus.BAD_REQUEST,
                "Validation failed",
                errors,
                e.getClass().getSimpleName(),
                request.getRequestURI());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException e, HttpServletRequest request) {
        String attemptedEmail = request.getParameter("email");

        return buildErrorResponse(HttpStatus.CONFLICT,
                "Email already in use",
                List.of("Email '" + attemptedEmail + "' already in use"),
                e.getClass().getSimpleName(),
                request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT,
                "Database constraint violated: duplicate value",
                List.of(e.getMostSpecificCause().getMessage()),
                e.getClass().getSimpleName(),
                request.getRequestURI());
    }

    // ------------------- Helper method to build ErrorResponse -------------------
    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message, List<String> details, String exception, String path) {
        ErrorResponse error = ErrorResponse.builder()
                .status(status.value())
                .message(message)
                .details(details)
                .exception(exception)
                .path(path)
                .build();

        return ResponseEntity.status(status).body(error);
    }
}
