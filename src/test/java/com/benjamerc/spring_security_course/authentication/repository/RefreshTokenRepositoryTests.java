package com.benjamerc.spring_security_course.authentication.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class RefreshTokenRepository {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void shouldReturnRefreshTokenByToken() {


    }
}
