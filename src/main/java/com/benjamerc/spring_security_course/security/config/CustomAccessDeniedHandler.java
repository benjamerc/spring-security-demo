package com.benjamerc.spring_security_course.security.config;

import com.benjamerc.spring_security_course.shared.builder.ApiErrorBuilder;
import com.benjamerc.spring_security_course.shared.dto.error.ApiError;
import com.benjamerc.spring_security_course.shared.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ApiErrorBuilder apiErrorBuilder;
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {

        ApiError apiError = apiErrorBuilder.buildError(
                ErrorCode.FORBIDDEN,
                "Insufficient permissions",
                HttpServletResponse.SC_FORBIDDEN,
                request.getRequestURI()
        );

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(apiError));
    }
}
