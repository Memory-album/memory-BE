package com.min.i.memory_BE.domain.user.service;

import com.min.i.memory_BE.domain.user.dto.UserRegisterDto;
import com.min.i.memory_BE.domain.user.dto.UserRegisterResultDto;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 유저 저장
    public UserRegisterResultDto registerUser(UserRegisterDto userRegisterDto) {

        // 비밀번호 암호화
        String hashedPassword = passwordEncoder.encode(userRegisterDto.getPassword());
        userRegisterDto.setPassword(hashedPassword);  // 암호화된 비밀번호로 덮어쓰기

        // User 객체
        User newUser = User.builder()
                .email(userRegisterDto.getEmail())
                .password(hashedPassword)  // 암호화된 비밀번호 사용
                .name(userRegisterDto.getName())
                .profileImageUrl(userRegisterDto.getProfileImgUrl())
                .build();

        // 이미 존재하는 이메일인지 체크 (중복 가입 방지)
        if (userRepository.existsByEmail(newUser.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 유저 저장
        userRepository.save(newUser);

        // 결과 반환 (DTO 반환)
        UserRegisterResultDto result = new UserRegisterResultDto();
        result.setMessage("회원가입 성공");
        result.setStatus("success");
        return result;
    }
}
