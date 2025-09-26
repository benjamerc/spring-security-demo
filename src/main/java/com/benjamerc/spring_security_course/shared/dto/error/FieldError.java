package com.benjamerc.spring_security_course.shared.dto.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldError {

    private String field;

    private Object rejectedValue;

    private String message;
}
