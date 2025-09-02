package com.benjamerc.SimpleLogin.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UserNotFoundByEmailException extends RuntimeException {
    public UserNotFoundByEmailException(String message) {
        super(message);
    }

    public UserNotFoundByEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
