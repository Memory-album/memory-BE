package com.min.i.memory_BE.domain.user.dto;

import lombok.Data;

@Data
public class PasswordResetDto {
    private String email;
    private String verificationCode;  // 이메일로 받은 인증 코드
    private String newPassword;       // 새로운 비밀번호
} 