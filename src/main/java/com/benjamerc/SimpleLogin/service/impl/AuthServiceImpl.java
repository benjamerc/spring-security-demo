package com.benjamerc.SimpleLogin.service.impl;

import com.benjamerc.SimpleLogin.domain.entity.UserEntity;
import com.benjamerc.SimpleLogin.domain.enums.Role;
import com.benjamerc.SimpleLogin.exception.EmailAlreadyExistsException;
import com.benjamerc.SimpleLogin.exception.InvalidCredentialsException;
import com.benjamerc.SimpleLogin.exception.UserNotFoundByEmailException;
import com.benjamerc.SimpleLogin.repository.UserRepository;
import com.benjamerc.SimpleLogin.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
    public UserEntity login(UserEntity loginAttempt) {
        UserEntity user = userRepository.findByEmail(loginAttempt.getEmail())
                .orElseThrow(UserNotFoundByEmailException::new);

        if (!passwordEncoder.matches(loginAttempt.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
}
