package com.benjamerc.spring_security_course.exception.token;

public class RefreshTokenRevokedException extends RuntimeException {

    public RefreshTokenRevokedException(String message) {
        super(message);
    }
}
