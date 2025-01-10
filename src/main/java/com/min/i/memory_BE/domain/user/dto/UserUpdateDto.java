package com.min.i.memory_BE.domain.user.dto;

import lombok.Data;

@Data
public class UserUpdateDto {
    private String email;
    private String newPassword;    // 새 비밀번호 (선택)
    private String name;           // 변경할 이름 (선택)
    private String profileImgUrl;  // 변경할 프로필 이미지 URL (선택)
} 