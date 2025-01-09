package com.min.i.memory_BE.domain.user.dto;

import lombok.Data;

@Data
public class UserUpdateDto {
    private String email;
    private String currentPassword;  // 현재 비밀번호
    private String newPassword;
    private String name;
    private String profileImgUrl;
} 