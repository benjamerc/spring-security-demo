package com.benjamerc.spring_security_course.advice;

import com.benjamerc.spring_security_course.domain.dto.error.ApiError;
import com.benjamerc.spring_security_course.exception.ErrorCode;
import com.benjamerc.spring_security_course.exception.token.refresh.HashingException;
import com.benjamerc.spring_security_course.exception.token.refresh.RefreshTokenExpiredException;
import com.benjamerc.spring_security_course.exception.token.refresh.RefreshTokenNotFoundException;
import com.benjamerc.spring_security_course.exception.token.refresh.RefreshTokenRevokedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RefreshTokenExceptionHandler {

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ApiError> handleRefreshTokenNotFound(RefreshTokenNotFoundException ex, HttpServletRequest request) {

        ApiError error = ApiError.builder()
                .code(ErrorCode.REFRESH_TOKEN_NOT_FOUND)
                .message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ApiError> handleRefreshTokenExpired(RefreshTokenExpiredException ex, HttpServletRequest request) {

        ApiError error = ApiError.builder()
                .code(ErrorCode.REFRESH_TOKEN_EXPIRED)
                .message(ex.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(RefreshTokenRevokedException.class)
    public ResponseEntity<ApiError> handleRefreshTokenRevoked(RefreshTokenRevokedException ex, HttpServletRequest request) {

        ApiError error = ApiError.builder()
                .code(ErrorCode.REFRESH_TOKEN_REVOKED)
                .message(ex.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(HashingException.class)
    public ResponseEntity<ApiError> handleHashingError(HashingException ex, HttpServletRequest request) {

        ApiError error = ApiError.builder()
                .code(ErrorCode.HASHING_EXCEPTION)
                .message(ex.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
