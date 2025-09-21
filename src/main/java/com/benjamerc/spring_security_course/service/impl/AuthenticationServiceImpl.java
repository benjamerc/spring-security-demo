package com.benjamerc.spring_security_course.service.impl;

import com.benjamerc.spring_security_course.domain.dto.auth.request.AuthAuthenticateRequest;
import com.benjamerc.spring_security_course.domain.dto.auth.request.AuthRefreshTokenRequest;
import com.benjamerc.spring_security_course.domain.dto.auth.request.AuthRegisterRequest;
import com.benjamerc.spring_security_course.domain.dto.auth.response.AuthAuthenticateResponse;
import com.benjamerc.spring_security_course.domain.dto.auth.response.AuthRegisterResponse;
import com.benjamerc.spring_security_course.domain.entity.RefreshToken;
import com.benjamerc.spring_security_course.domain.entity.User;
import com.benjamerc.spring_security_course.mapper.AuthenticationMapper;
import com.benjamerc.spring_security_course.repository.UserRepository;
import com.benjamerc.spring_security_course.security.CustomUserDetails;
import com.benjamerc.spring_security_course.security.AccessTokenService;
import com.benjamerc.spring_security_course.security.RefreshTokenService;
import com.benjamerc.spring_security_course.security.Role;
import com.benjamerc.spring_security_course.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationMapper authenticationMapper;
    private final AuthenticationManager authenticationManager;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public AuthRegisterResponse register(AuthRegisterRequest request) {

        User user = User.builder()
                .username(request.username())
                .name(request.name())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        return authenticationMapper.toAuthRegisterResponse(userRepository.save(user));
    }

    @Override
    public AuthAuthenticateResponse login(AuthAuthenticateRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        UUID session = UUID.randomUUID();

        String accessToken = accessTokenService.createAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, session);

        return new AuthAuthenticateResponse(accessToken, refreshToken.getToken());
    }

    @Override
    public AuthAuthenticateResponse refreshToken(AuthRefreshTokenRequest request) {

        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(request.token());
        String newAccessToken = accessTokenService.createAccessToken(newRefreshToken.getUser());

        return new AuthAuthenticateResponse(newAccessToken, newRefreshToken.getToken());
    }

    @Override
    public void logout(AuthRefreshTokenRequest request) {

        refreshTokenService.revokeRefreshToken(request.token());
    }
}
