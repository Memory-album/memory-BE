package com.min.i.memory_BE.domain.user.dto;

import com.min.i.memory_BE.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String profileImgUrl;
    private boolean emailVerified;
    private String status;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .profileImgUrl(user.getProfileImgUrl())
                .emailVerified(user.isEmailVerified())
                .status(user.getStatus().name())
                .build();
    }
} 