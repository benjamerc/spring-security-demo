package com.benjamerc.spring_security_course.advice;

import com.benjamerc.spring_security_course.domain.dto.error.ApiError;
import com.benjamerc.spring_security_course.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AccessTokenExceptionHandler {

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiError> handleAccessTokenExpired(ExpiredJwtException ex, HttpServletRequest request) {

        ApiError error = ApiError.builder()
                .code(ErrorCode.ACCESS_TOKEN_EXPIRED)
                .message("The access token has expired")
                .status(HttpStatus.UNAUTHORIZED.value())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ApiError> handleAccessTokenSignature(SignatureException ex, HttpServletRequest request) {

        ApiError error = ApiError.builder()
                .code(ErrorCode.ACCESS_TOKEN_INVALID_SIGNATURE)
                .message("The access token signature is invalid")
                .status(HttpStatus.UNAUTHORIZED.value())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiError> handleAccessTokenGeneric(JwtException ex, HttpServletRequest request) {

        ApiError error = ApiError.builder()
                .code(ErrorCode.ACCESS_TOKEN_INVALID)
                .message("The access token is invalid")
                .status(HttpStatus.UNAUTHORIZED.value())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}
