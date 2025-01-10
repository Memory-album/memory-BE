package com.min.i.memory_BE.domain.user.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerificationEvent {
    private final String email;
    private final String name;
    private final EventType type;
    private String jwtToken;
    private boolean manualLogin;
    
    public enum EventType {
        VERIFICATION,
        WELCOME,
        PASSWORD_RESET,
        ACCOUNT_ACTIVATED,
        ACCOUNT_DEACTIVATED
    }
    
    public EmailVerificationEvent(String email, String name, EventType type) {
        this.email = email;
        this.name = name;
        this.type = type;
    }
} 