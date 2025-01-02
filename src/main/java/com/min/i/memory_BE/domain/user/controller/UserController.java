package com.min.i.memory_BE.domain.user.controller;

import com.min.i.memory_BE.domain.user.dto.UserRegisterDto;
import com.min.i.memory_BE.domain.user.dto.UserRegisterResultDto;
import com.min.i.memory_BE.domain.user.service.EmailService;
import com.min.i.memory_BE.domain.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    // 이메일 인증 코드 발송
    @PostMapping("/send-verification-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody UserRegisterDto userRegisterDto) {
        // JWT 생성 및 이메일 전송
        String jwt = emailService.sendVerificationCode(userRegisterDto);

        if (jwt != null) {
            return ResponseEntity.ok("인증 코드가 이메일로 전송되었습니다. JWT: " + jwt);
        } else {
            return ResponseEntity.status(500).body("이메일 전송에 실패했습니다.");
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestBody UserRegisterDto userRegisterDto,
                                              @RequestHeader("Authorization") String authorization) {
        // JWT 토큰 추출 (Bearer 토큰에서 실제 JWT 값을 추출)
        String jwtToken = authorization.replace("Bearer ", "");

        // JWT 유효성 검사 후 이메일 인증
        String newJwt = userService.verifyEmail(jwtToken, userRegisterDto.getEmailVerificationCode());

        if (newJwt != null) {
            return ResponseEntity.ok("이메일 인증에 성공했습니다. New JWT: " + newJwt);
        } else {
            return ResponseEntity.status(400).body("이메일 인증에 실패했습니다. 인증 코드를 다시 확인하세요.");
        }
    }

    // 사용자 정보 추가 입력 후 최종 회원가입 처리
    @PostMapping("/complete-register")
    public ResponseEntity<String> completeRegister(@RequestBody UserRegisterDto userRegisterDto, @RequestHeader("Authorization") String authorization) {

        // JWT 토큰 추출 (Bearer 토큰에서 실제 JWT 값을 추출)
        String jwtToken = authorization.replace("Bearer ", "");

        // 인증된 이메일로 최종 회원가입 처리
        UserRegisterResultDto result = userService.completeRegister(userRegisterDto, jwtToken);

        return ResponseEntity.ok(result.getMessage());
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
