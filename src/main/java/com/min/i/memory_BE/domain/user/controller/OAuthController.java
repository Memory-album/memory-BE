package com.min.i.memory_BE.domain.user.controller;

import com.min.i.memory_BE.domain.user.enums.OAuthProvider;
import com.min.i.memory_BE.domain.user.service.OAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth")
public class OAuthController {

    private final OAuthService oAuthService;

    public OAuthController(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    @GetMapping("/login")
    public ResponseEntity<Void> redirectToLogin(@RequestParam("provider") String providerName) {
        OAuthProvider provider = OAuthProvider.valueOf(providerName.toUpperCase());
        String authUrl = oAuthService.generateAuthUrl(provider);
        return ResponseEntity.status(HttpStatus.FOUND).header("Location", authUrl).build();
    }

    @GetMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestParam("provider") String providerName,
                                                 @RequestParam("code") String code,
                                                 @RequestParam(value = "state", required = false) String state) {
        OAuthProvider provider = OAuthProvider.valueOf(providerName.toUpperCase());
        oAuthService.handleCallback(provider, code, state);
        return ResponseEntity.ok(providerName + " 로그인 성공");
    }
}

