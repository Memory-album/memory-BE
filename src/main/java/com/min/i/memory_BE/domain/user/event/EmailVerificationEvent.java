package com.min.i.memory_BE.domain.user.event;

import lombok.Getter;

@Getter
public class EmailVerificationEvent {
    private final String email;
    
    public EmailVerificationEvent(String email) {
        this.email = email;
    }
} 