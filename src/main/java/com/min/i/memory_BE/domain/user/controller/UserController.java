package com.min.i.memory_BE.domain.user.controller;

import com.min.i.memory_BE.domain.user.dto.UserRegisterDto;
import com.min.i.memory_BE.domain.user.dto.UserRegisterResultDto;
import com.min.i.memory_BE.domain.user.service.EmailService;
import com.min.i.memory_BE.domain.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    // 회원가입 처리
    @PostMapping("/register")
    public ResponseEntity<UserRegisterResultDto> register(@RequestBody UserRegisterDto userRegisterDto) {

        // 이메일 인증 코드 발송
        emailService.sendVerificationCode(userRegisterDto);

        // 인증 코드 전송 후 바로 성공 응답 반환
        UserRegisterResultDto result = new UserRegisterResultDto();
        result.setMessage("이메일 인증 코드가 전송되었습니다.");
        result.setStatus("success");
        return ResponseEntity.status(201).body(result);
    }

    // 이메일 인증 코드 확인 (POST /user/verify-email)
    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestBody UserRegisterDto userRegisterDto) {
        boolean isVerified = userService.verifyEmail(userRegisterDto);

        if (isVerified) {
            return ResponseEntity.ok("이메일 인증에 성공했습니다.");
        } else {
            return ResponseEntity.status(400).body("이메일 인증에 실패했습니다. 인증 코드를 다시 확인하세요.");
        }
    }

    // 사용자 정보 추가 입력 후 최종 회원가입 처리
    @PostMapping("/complete-register")
    public ResponseEntity<String> completeRegister(@RequestBody UserRegisterDto userRegisterDto) {
        // 인증된 이메일로 최종 회원가입 처리
        userService.completeRegister(userRegisterDto);

        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }


    // 로그인 페이지
    @GetMapping("/loginPage")
    public ResponseEntity<String> loginPage() {
        return ResponseEntity.ok("로그인 페이지로 이동");

    }

    // 홈 페이지
    @GetMapping("/home")
    public ResponseEntity<String> homePage() {
        return ResponseEntity.ok("홈으로 이동");
    }

    // 마이 페이지
    @GetMapping("/my-page")
    public ResponseEntity<String> myPage(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            return ResponseEntity.ok("마이 페이지: " + auth.getName());
        } else {
            return ResponseEntity.status(401).body("로그인 필요");
        }
    }
}
