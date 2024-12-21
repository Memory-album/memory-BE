package com.min.i.memory_BE.domain.user.service;

import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.repository.UserRepository;
import com.min.i.memory_BE.global.config.SecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final PasswordEncoder passwordEncoder;  // PasswordEncoder 주입


    @Autowired
    public MyUserDetailsService(UserRepository userRepository,  PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;  // 주입
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Attempting to load user by email: {}", email);  // 이메일 확인용 로그
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        logger.debug("User found: {}", user.getEmail());
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("USER")))
                .build();
    }
}
