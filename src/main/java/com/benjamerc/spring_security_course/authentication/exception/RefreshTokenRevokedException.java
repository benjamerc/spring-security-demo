package com.benjamerc.spring_security_course.authentication.exception;

public class RefreshTokenRevokedException extends RuntimeException {

    public RefreshTokenRevokedException(String message) {
        super(message);
    }
}
