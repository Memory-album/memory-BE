package com.min.i.memory_BE.domain.user.controller;

import com.min.i.memory_BE.domain.user.service.OAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class OAuthController {

    private final OAuthService oAuthService;

    public OAuthController(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    @GetMapping("/login/naver")
    public ResponseEntity<String> redirectToNaverLogin() {
        String naverAuthUrl = oAuthService.generateNaverAuthUrl();
        return ResponseEntity.status(HttpStatus.FOUND).header("Location", naverAuthUrl).build();
    }

    @GetMapping("/callback/naver")
    public ResponseEntity<String> handleNaverCallback(@RequestParam("code") String code,
                                                      @RequestParam("state") String state) {
        oAuthService.handleNaverCallback(code, state);
        return ResponseEntity.ok("로그인 성공");
    }
}

