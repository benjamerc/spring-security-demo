package com.benjamerc.spring_security_course.shared.advice;

import com.benjamerc.spring_security_course.shared.dto.error.ApiError;
import com.benjamerc.spring_security_course.shared.dto.error.FieldError;
import com.benjamerc.spring_security_course.shared.exception.ErrorCode;
import com.benjamerc.spring_security_course.authentication.exception.HashingException;
import com.benjamerc.spring_security_course.authentication.exception.RefreshTokenExpiredException;
import com.benjamerc.spring_security_course.authentication.exception.RefreshTokenNotFoundException;
import com.benjamerc.spring_security_course.authentication.exception.RefreshTokenRevokedException;
import com.benjamerc.spring_security_course.users.exception.UserNotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ===================== Security Exceptions =====================

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiError> handleAccessTokenExpired(ExpiredJwtException ex, HttpServletRequest request) {

        return buildErrorResponse(
                ErrorCode.ACCESS_TOKEN_EXPIRED,
                "The access token has expired",
                HttpStatus.UNAUTHORIZED,
                request
        );
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ApiError> handleAccessTokenSignature(SignatureException ex, HttpServletRequest request) {

        return buildErrorResponse(
                ErrorCode.ACCESS_TOKEN_INVALID_SIGNATURE,
                "The access token signature is invalid",
                HttpStatus.UNAUTHORIZED,
                request
        );
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiError> handleAccessTokenGeneric(JwtException ex, HttpServletRequest request) {

        return buildErrorResponse(
                ErrorCode.ACCESS_TOKEN_INVALID,
                "The access token is invalid",
                HttpStatus.UNAUTHORIZED,
                request
        );
    }

    // ===================== Refresh Token Exceptions =====================

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ApiError> handleRefreshTokenNotFound(RefreshTokenNotFoundException ex, HttpServletRequest request) {

        return buildErrorResponse(
                ErrorCode.REFRESH_TOKEN_NOT_FOUND,
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                request
        );
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ApiError> handleRefreshTokenExpired(RefreshTokenExpiredException ex, HttpServletRequest request) {

        return buildErrorResponse(
                ErrorCode.REFRESH_TOKEN_EXPIRED,
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED,
                request
        );
    }

    @ExceptionHandler(RefreshTokenRevokedException.class)
    public ResponseEntity<ApiError> handleRefreshTokenRevoked(RefreshTokenRevokedException ex, HttpServletRequest request) {

        return buildErrorResponse(
                ErrorCode.REFRESH_TOKEN_REVOKED,
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED,
                request
        );
    }

    @ExceptionHandler(HashingException.class)
    public ResponseEntity<ApiError> handleHashingError(HashingException ex, HttpServletRequest request) {

        return buildErrorResponse(
                ErrorCode.HASHING_EXCEPTION,
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }

    // ===================== User Exceptions =====================

    @ExceptionHandler({UserNotFoundException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiError> handleUserNotFoundExceptions(RuntimeException ex, HttpServletRequest request) {

        return buildErrorResponse(
                ErrorCode.USER_NOT_FOUND,
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                request
        );
    }

    // ===================== Generic Exceptions =====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {

        BindingResult result = ex.getBindingResult();

        List<FieldError> fieldErrors = result.getFieldErrors().stream()
                .map(fe -> FieldError.builder()
                        .field(fe.getField())
                        .rejectedValue(fe.getRejectedValue())
                        .message(fe.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        return buildErrorResponse(
                ErrorCode.VALIDATION_ERROR,
                "Validation failed",
                HttpStatus.BAD_REQUEST,
                request,
                fieldErrors
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {

        String message = "Database error";

        if (ex.getCause() instanceof ConstraintViolationException) {
            message = "Username already exists";
        }

        return buildErrorResponse(
                ErrorCode.USERNAME_ALREADY_EXISTS,
                message,
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {

        log.error("Unhandled exception", ex);

        return buildErrorResponse(
                ErrorCode.INTERNAL_ERROR,
                "Internal error",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception", ex);

        return buildErrorResponse(
                ErrorCode.UNEXPECTED_ERROR,
                "Something went wrong",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }

    private ResponseEntity<ApiError> buildErrorResponse(
            ErrorCode code,
            String message,
            HttpStatus status,
            HttpServletRequest request
    ) {
        ApiError error = ApiError.builder()
                .code(code)
                .message(message)
                .status(status.value())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(error);
    }

    private ResponseEntity<ApiError> buildErrorResponse(
            ErrorCode code,
            String message,
            HttpStatus status,
            HttpServletRequest request,
            List<FieldError> details
    ) {
        ApiError error = ApiError.builder()
                .code(code)
                .message(message)
                .status(status.value())
                .path(request.getRequestURI())
                .details(details)
                .build();

        return ResponseEntity.status(status).body(error);
    }
}
