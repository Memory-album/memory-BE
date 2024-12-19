package com.min.i.memory_BE.domain.user.enums;

import lombok.Getter;

@Getter
public enum UserMailStatus {
    UNVERIFIED("미인증"),
    VERIFIED("인증완료"),
    REGISTERED("가입완료");

    private final String description;

    UserMailStatus(String description) {
        this.description = description;
    }
}
