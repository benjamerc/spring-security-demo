package com.benjamerc.spring_security_course.security;

import com.benjamerc.spring_security_course.exception.token.refresh.HashingException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class TokenUtils {

    public static String hashSHA256(String input) {
        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashed = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hashed);

        } catch (NoSuchAlgorithmException e) {

            throw new HashingException("Error hashing input", e);
        }
    }
}
