package com.benjamerc.spring_security_course.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${ALLOWED_ORIGINS}")
    private String allowedOrigins;

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {

        List<String> origins = Arrays.asList(allowedOrigins.split(","));

        CorsConfiguration publicConfig = new CorsConfiguration();
        publicConfig.setAllowedOriginPatterns(origins);
        publicConfig.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        publicConfig.setAllowedHeaders(List.of("Content-Type", "Accept", "Origin"));

        CorsConfiguration privateConfig = new CorsConfiguration();
        privateConfig.setAllowedOriginPatterns(origins);
        privateConfig.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        privateConfig.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin"));
        privateConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/auth/**", publicConfig);
        source.registerCorsConfiguration("/**", privateConfig);

        return source;
    }
}
