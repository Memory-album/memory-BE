package com.min.i.memory_BE.domain.user.dto;

import lombok.Data;

@Data
public class UserRegisterDto {
    private String email;
    private String password;
    private String name;
    private String profileImgUrl;

    // Getters and Setters
}

