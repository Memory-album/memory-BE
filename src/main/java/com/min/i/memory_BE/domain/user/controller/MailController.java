package com.min.i.memory_BE.domain.user.controller;

import com.min.i.memory_BE.domain.user.dto.UserRegisterDto;
import com.min.i.memory_BE.domain.user.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mail")
public class MailController {

    @Autowired
    private EmailService emailService;

    // 이메일 인증 코드 발송
    @PostMapping("/send-verification-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody UserRegisterDto userRegisterDto) {
        boolean isCodeSent = emailService.sendVerificationCode(userRegisterDto);

        if (isCodeSent) {
            return ResponseEntity.ok("인증 코드가 이메일로 전송되었습니다.");
        } else {
            return ResponseEntity.status(500).body("이메일 전송에 실패했습니다.");
        }
    }
}

