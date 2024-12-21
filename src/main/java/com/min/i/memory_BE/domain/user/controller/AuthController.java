package com.min.i.memory_BE.domain.user.controller;

import com.min.i.memory_BE.domain.user.dto.JwtAuthenticationResponse;
import com.min.i.memory_BE.domain.user.dto.UserLoginDto;
import com.min.i.memory_BE.domain.user.service.JwtTokenProvider;
import com.min.i.memory_BE.domain.user.service.UserService;
import com.min.i.memory_BE.global.config.SecurityConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> login(@RequestBody UserLoginDto loginDto) {
       try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getEmail(),
                            loginDto.getPassword()
                    )
            );

           // 액세스 토큰 생성
           String token = jwtTokenProvider.generateToken(loginDto.getEmail());
           // 리프레시 토큰 생성
           String refreshToken = jwtTokenProvider.generateRefreshToken(loginDto.getEmail());

           // 토큰을 반환
           logger.debug("생성된 토큰: {}", token);
           return ResponseEntity.ok(new JwtAuthenticationResponse(token, refreshToken));

    } catch (Exception e) {
            logger.error("로그인 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        // 클라이언트가 토큰 삭제하도록 알림
        return ResponseEntity.ok("로그아웃 성공");
    }

    // 리프레시 토큰을 이용해 새로운 액세스 토큰 발급
    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthenticationResponse> refresh(@RequestBody String refreshToken) {
        if (jwtTokenProvider.validateRefreshToken(refreshToken)) {
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            String newToken = jwtTokenProvider.generateToken(username);
            return ResponseEntity.ok(new JwtAuthenticationResponse(newToken, refreshToken));
        } else {
            return ResponseEntity.status(401).body(null); // 리프레시 토큰이 유효하지 않으면 401 응답
        }
    }

}
