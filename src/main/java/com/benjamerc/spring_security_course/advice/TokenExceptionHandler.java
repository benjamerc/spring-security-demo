package com.benjamerc.spring_security_course.advice;

import com.benjamerc.spring_security_course.domain.dto.error.ApiError;
import com.benjamerc.spring_security_course.exception.ErrorCode;
import com.benjamerc.spring_security_course.exception.token.RefreshTokenNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TokenExceptionHandler {

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
}
