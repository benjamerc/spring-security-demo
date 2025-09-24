package com.benjamerc.spring_security_course.exception.token.refresh;

public class RefreshTokenRevokedException extends RuntimeException {

    public RefreshTokenRevokedException(String message) {
        super(message);
    }
}
