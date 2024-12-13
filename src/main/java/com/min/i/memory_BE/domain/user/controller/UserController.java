package com.min.i.memory_BE.domain.user.controller;

import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // 회원가입 페이지로 이동
    @GetMapping("/register")
    public String showRegisterForm() {
        return "register"; // 회원가입 페이지 뷰
    }

    // 회원가입 처리
    @PostMapping("/register")
    public String registerUser(@RequestBody User user) {
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(user.getPassword());

        // User 객체 생성 (Builder 패턴 사용)
        user = User.builder()
                .email(user.getEmail())
                .password(encodedPassword)  // 암호화된 비밀번호 설정
                .name(user.getName())
                .profileImageUrl(user.getProfileImgUrl())
                .build();

        // 유저 서비스에서 유저 저장
        userService.saveUser(user);

        return "회원가입이 완료되었습니다.";  // JSON 응답으로 처리

//        model.addAttribute("message", "회원가입이 완료되었습니다.");
//        return "login"; // 로그인 페이지로 리다이렉트
    }

}
