package com.benjamerc.spring_security_course.domain.dto.error;

import com.benjamerc.spring_security_course.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiError {

    private ErrorCode code;

    private String message;

    @Builder.Default
    private Instant timestamp = Instant.now();

    private int status;

    private String path;

    @Builder.Default
    private List<FieldError> details = new ArrayList<>();
}
