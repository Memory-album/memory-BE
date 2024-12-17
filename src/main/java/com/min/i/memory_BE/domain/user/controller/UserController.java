package com.min.i.memory_BE.domain.user.controller;

import com.min.i.memory_BE.domain.user.dto.UserRegisterDto;
import com.min.i.memory_BE.domain.user.dto.UserRegisterResultDto;
import com.min.i.memory_BE.domain.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 회원가입 처리
    @PostMapping("/register")
    public ResponseEntity<UserRegisterResultDto> register(@RequestBody UserRegisterDto userRegisterDto) {

        // 회원가입 처리 후 결과 반환
        UserRegisterResultDto result = userService.registerUser(userRegisterDto);

        SecurityContextHolder.clearContext();

        // 성공 응답 반환
        return ResponseEntity.status(201).body(result);
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
