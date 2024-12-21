package com.min.i.memory_BE.domain.user.dto;

import com.min.i.memory_BE.domain.user.enums.UserMailStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserRegisterDto {
    private String email;
    private String password;
    private String name;
    private String profileImgUrl;
    private UserMailStatus mailStatus;
    private String emailVerificationCode; // 인증 코드
    private LocalDateTime emailVerificationExpiredAt; // 인증 코드 유효 시간
}

