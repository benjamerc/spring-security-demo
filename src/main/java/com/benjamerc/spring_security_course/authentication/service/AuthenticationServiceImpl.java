package com.benjamerc.spring_security_course.authentication.service;

import com.benjamerc.spring_security_course.authentication.dto.request.AuthAuthenticateRequest;
import com.benjamerc.spring_security_course.authentication.dto.request.AuthRefreshTokenRequest;
import com.benjamerc.spring_security_course.authentication.dto.request.AuthRegisterRequest;
import com.benjamerc.spring_security_course.authentication.dto.response.AuthAuthenticateResponse;
import com.benjamerc.spring_security_course.authentication.dto.response.AuthRegisterResponse;
import com.benjamerc.spring_security_course.authentication.dto.token.RefreshTokenWithRaw;
import com.benjamerc.spring_security_course.authentication.model.RefreshToken;
import com.benjamerc.spring_security_course.users.model.User;
import com.benjamerc.spring_security_course.authentication.mapper.AuthenticationMapper;
import com.benjamerc.spring_security_course.users.repository.UserRepository;
import com.benjamerc.spring_security_course.security.core.CustomUserDetails;
import com.benjamerc.spring_security_course.security.core.AccessTokenService;
import com.benjamerc.spring_security_course.authentication.security.RefreshTokenService;
import com.benjamerc.spring_security_course.security.core.Role;
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
        RefreshTokenWithRaw refreshToken = refreshTokenService.createRefreshToken(user, session);

        return new AuthAuthenticateResponse(accessToken, refreshToken.getRawToken());
    }

    @Override
    public AuthAuthenticateResponse refreshToken(AuthRefreshTokenRequest request) {

        RefreshTokenWithRaw newRefreshToken = refreshTokenService.rotateRefreshToken(request.token());
        String newAccessToken = accessTokenService.createAccessToken(newRefreshToken.getRefreshToken().getUser());

        return new AuthAuthenticateResponse(newAccessToken, newRefreshToken.getRawToken());
    }

    @Override
    public void logout(AuthRefreshTokenRequest request) {

        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.token());

        refreshTokenService.revokeTokensBySession(refreshToken.getUser(), refreshToken.getSession());
    }
}
