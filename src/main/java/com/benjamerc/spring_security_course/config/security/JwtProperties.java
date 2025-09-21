package com.benjamerc.spring_security_course.config.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.security.jwt")
@Data
public class JwtProperties {

    private String secretKey;

    private long expiration;

    private RefreshToken refreshToken;

    @Data
    public static class RefreshToken {
        private long expiration;
    }
}
