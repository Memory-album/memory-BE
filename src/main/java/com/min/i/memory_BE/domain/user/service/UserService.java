package com.min.i.memory_BE.domain.user.service;

import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 유저 저장
    public void saveUser(User user) {

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(user.getPassword());

        // User 객체 생성 시 Builder 패턴으로 비밀번호 설정
        user = User.builder()
                .email(user.getEmail())
                .password(encodedPassword)
                .name(user.getName())
                .profileImageUrl(user.getProfileImgUrl())
                .dateOfBirth(user.getDateOfBirth())
                .build();

        // 이미 존재하는 이메일인지 체크 (중복 가입 방지)
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 유저 저장
        userRepository.save(user);
    }
}
