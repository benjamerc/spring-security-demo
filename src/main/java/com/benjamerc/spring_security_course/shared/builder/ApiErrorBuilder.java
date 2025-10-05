package com.benjamerc.spring_security_course.shared.builder;

import com.benjamerc.spring_security_course.shared.dto.error.ApiError;
import com.benjamerc.spring_security_course.shared.dto.error.FieldError;
import com.benjamerc.spring_security_course.shared.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ApiErrorBuilder {

    public ApiError buildError(ErrorCode code, String message, int status, String path) {

        return ApiError.builder()
                .code(code)
                .message(message)
                .status(status)
                .path(path)
                .build();
    }

    public ApiError buildError(ErrorCode code, String message, int status, String path, List<FieldError> details) {

        return ApiError.builder()
                .code(code)
                .message(message)
                .status(status)
                .path(path)
                .details(details)
                .build();
    }

    public ResponseEntity<ApiError> build(ErrorCode code, String message, HttpStatus status, HttpServletRequest request) {

        return ResponseEntity.status(status).body(buildError(code, message, status.value(), request.getRequestURI()));
    }

    public ResponseEntity<ApiError> build(ErrorCode code, String message, HttpStatus status, HttpServletRequest request, List<FieldError> details) {

        return ResponseEntity.status(status).body(buildError(code, message, status.value(), request.getRequestURI(), details));
    }

    public List<FieldError> buildFieldErrors(BindingResult result) {

        return result.getFieldErrors().stream()
                .map(fe -> FieldError.builder()
                        .field(fe.getField())
                        .rejectedValue(fe.getRejectedValue())
                        .message(fe.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());
    }
}
