package com.min.i.memory_BE.domain.user.dto;

import lombok.Data;

@Data
public class UserLoginDto {

    private String email;
    private String password;
    private boolean rememberMe; //자동 로그인 여부
}
