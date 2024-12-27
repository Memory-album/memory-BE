package com.min.i.memory_BE.domain.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserRegisterDto {
    private String email;
    private String password;
    private String name;
    private String profileImgUrl;
    private String emailVerificationCode; // 인증 코드
}

