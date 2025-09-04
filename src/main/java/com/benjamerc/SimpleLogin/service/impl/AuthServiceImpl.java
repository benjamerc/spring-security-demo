package com.benjamerc.SimpleLogin.service.impl;

import com.benjamerc.SimpleLogin.domain.dto.auth.request.UserLoginRequest;
import com.benjamerc.SimpleLogin.domain.dto.auth.response.UserLoginResponse;
import com.benjamerc.SimpleLogin.domain.entity.UserEntity;
import com.benjamerc.SimpleLogin.domain.enums.Role;
import com.benjamerc.SimpleLogin.exception.EmailAlreadyExistsException;
import com.benjamerc.SimpleLogin.exception.InvalidCredentialsException;
import com.benjamerc.SimpleLogin.exception.UserNotFoundByEmailException;
import com.benjamerc.SimpleLogin.mapper.AuthMapper;
import com.benjamerc.SimpleLogin.repository.UserRepository;
import com.benjamerc.SimpleLogin.security.JwtService;
import com.benjamerc.SimpleLogin.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthMapper authMapper;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           JwtService jwtService, AuthMapper authMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authMapper = authMapper;
    }

    @Override
    public UserEntity register(UserEntity registerAttempt) {
        if (userRepository.existsByEmail(registerAttempt.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        registerAttempt.setPassword(passwordEncoder.encode(registerAttempt.getPassword()));
        registerAttempt.setRole(Role.ROLE_USER);
        return userRepository.save(registerAttempt);
    }

    @Override
    public UserLoginResponse login(UserLoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(UserNotFoundByEmailException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getEmail());

        UserLoginResponse response = authMapper.entityToLoginResponse(user);
        response.setToken(token);

        return response;
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
}
