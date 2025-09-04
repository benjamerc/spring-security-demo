package com.benjamerc.SimpleLogin.service.impl;

import com.benjamerc.SimpleLogin.exception.UserNotFoundByEmailException;
import com.benjamerc.SimpleLogin.repository.UserRepository;
import com.benjamerc.SimpleLogin.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(UserPrincipal::new)
                .orElseThrow(UserNotFoundByEmailException::new);
//                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
