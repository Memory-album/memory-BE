package com.min.i.memory_BE.domain.user.service;

import com.min.i.memory_BE.domain.user.dto.UserRegisterDto;
import com.min.i.memory_BE.domain.user.dto.UserRegisterResultDto;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.enums.UserMailStatus;
import com.min.i.memory_BE.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 이메일 인증 확인 및 코드 검증
    public boolean verifyEmail(UserRegisterDto userRegisterDto) {
        // 이메일로 임시 사용자 검색
        User user = userRepository.findByEmail(userRegisterDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 사용자가 없습니다."));

        // 인증 코드 검증
        if (user.getEmailVerificationCode().equals(userRegisterDto.getEmailVerificationCode()) &&
                LocalDateTime.now().isBefore(user.getEmailVerificationExpiredAt())) {

            // 인증 성공 후, 상태를 VERIFIED로 변경
            user.completeEmailVerification();

            userRepository.save(user);
            return true;
        }

        return false; // 인증 실패
    }

    // 회원가입을 위한 최종 처리
    public UserRegisterResultDto completeRegister(UserRegisterDto userRegisterDto) {

        // 임시 사용자 검색
        User tempUser = userRepository.findByEmail(userRegisterDto.getEmail())
                .filter(user -> user.getMailStatus() == UserMailStatus.VERIFIED)  // 이메일 인증이 완료된 사용자만
                .orElseThrow(() -> new IllegalArgumentException("이메일 인증을 먼저 완료해야 합니다."));

        // 비밀번호 암호화
        String hashedPassword = passwordEncoder.encode(userRegisterDto.getPassword());
        userRegisterDto.setPassword(hashedPassword);  // 암호화된 비밀번호로 덮어쓰기

        // 최종 사용자로 업데이트 (이름, 프로필 이미지, 비밀번호 등)
        tempUser.completeRegistration(userRegisterDto.getPassword(), userRegisterDto.getName(), userRegisterDto.getProfileImgUrl());

        // 최종 사용자로 저장
        userRepository.save(tempUser);

        // 결과 반환 (DTO 반환)
        UserRegisterResultDto result = new UserRegisterResultDto();
        result.setMessage("회원가입 성공");
        result.setStatus("success");
        return result;
    }
}
