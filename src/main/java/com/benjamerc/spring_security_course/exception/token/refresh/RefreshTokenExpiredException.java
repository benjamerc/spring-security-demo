package com.benjamerc.spring_security_course.exception.token.refresh;

public class RefreshTokenExpiredException extends RuntimeException {

    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}
