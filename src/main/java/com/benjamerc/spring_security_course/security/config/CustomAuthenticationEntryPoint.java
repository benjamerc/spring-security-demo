package com.benjamerc.spring_security_course.security.config;

import com.benjamerc.spring_security_course.shared.builder.ApiErrorBuilder;
import com.benjamerc.spring_security_course.shared.dto.error.ApiError;
import com.benjamerc.spring_security_course.shared.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ApiErrorBuilder apiErrorBuilder;
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        ApiError apiError = apiErrorBuilder.buildError(
                ErrorCode.UNAUTHORIZED,
                "Unauthorized or invalid token",
                HttpServletResponse.SC_UNAUTHORIZED,
                request.getRequestURI()
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(apiError));
    }
}
