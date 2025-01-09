package com.min.i.memory_BE.domain.user.controller;

import com.min.i.memory_BE.domain.user.dto.JwtAuthenticationResponse;
import com.min.i.memory_BE.domain.user.dto.UserLoginDto;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.service.JwtTokenProvider;
import com.min.i.memory_BE.domain.user.service.UserService;
import com.min.i.memory_BE.global.config.SecurityConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
public class LoginController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private UserService userService;

    @Operation(
            summary = "로그인",
            description = "사용자의 이메일과 비밀번호를 사용하여 로그인하고 JWT 토큰을 발급합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 실패 (잘못된 이메일 또는 비밀번호)"
            ),
            @ApiResponse(
                    responseCode = "423",
                    description = "계정 잠금 (로그인 시도 횟수 초과)"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<String> login(@Parameter(description = "로그인에 필요한 사용자 이메일과 비밀번호")
                                        @RequestBody UserLoginDto loginDto,
                                        @Parameter(description = "HTTP 응답에 쿠키를 추가할 수 있도록 제공되는 HttpServletResponse 객체")
                                        HttpServletResponse response) {
        try {

            // 이메일이 존재하는지 확인
            User user = userService.getUserByEmail(loginDto.getEmail());
            if (user == null) {
                // 이메일이 존재하지 않으면 로그인 실패 처리
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("없는 이메일입니다.");
            }

            // 로그인 실패 횟수 체크 - Brute-force attack (무차별 대입 공격) 방지
            if (userService.isAccountLocked(loginDto.getEmail())) {
                // 계정 잠금 상태가 있고, 잠금 시간이 남아있다면 그 정보를 함께 반환
                LocalDateTime lockedUntil = user.getLockedUntil();
                long minutesLeft = Duration.between(LocalDateTime.now(), lockedUntil).toMinutes();

                return ResponseEntity.status(HttpStatus.LOCKED)
                        .body("계정이 잠겼습니다. " + minutesLeft + "분 후에 다시 시도해 주세요.");
            }

            try {
                // 인증 시도
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
                );
                
                // 인증 성공 시 토큰 생성 및 쿠키 설정
                JwtAuthenticationResponse tokens = userService.generateTokens(loginDto.getEmail());
                
                // JWT 쿠키 설정
                ResponseCookie accessTokenCookie = ResponseCookie.from("jwtToken", tokens.getAccessToken())
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(60 * 60)
                        .sameSite("Strict")
                        .build();

                ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(60 * 60 * 24 * 30)
                        .sameSite("Strict")
                        .build();

                response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
                response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

            logger.debug("생성된 액세스 토큰: {}", tokens.getAccessToken());
            logger.debug("생성된 리프레시 토큰: {}", tokens.getRefreshToken());

            return ResponseEntity.ok("로그인 성공");

            } catch (BadCredentialsException e) {
                // 비밀번호가 틀린 경우에만 로그인 시도 횟수 증가
                int attempts = userService.incrementLoginAttempts(loginDto.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("비밀번호가 틀렸습니다. 현재 로그인 시도 횟수: " + attempts + "회");
            }

        } catch (Exception e) {
            logger.error("로그인 실패: {}", e.getMessage());
            // 기타 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("로그인 처리 중 오류가 발생했습니다.");
        }
    }

    @Operation(
            summary = "로그아웃",
            description = "로그아웃을 수행하고 JWT 토큰이 저장된 쿠키를 삭제합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "로그아웃 성공",
            content = @Content(mediaType = "application/json")
    )
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {

        // JWT 쿠키 삭제를 위한 ResponseCookie 사용
        ResponseCookie cookie = ResponseCookie.from("jwtToken", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok("로그아웃 성공");
    }

    @Operation(
            summary = "리프레시 토큰을 이용해 새로운 액세스 토큰 발급",
            description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "새로운 액세스 토큰 발급 성공",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "리프레시 토큰이 유효하지 않음"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류"
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthenticationResponse> refresh(@Parameter(description = "리프레시 토큰")
                                                             @RequestBody String refreshToken) {
        try {
            if (jwtTokenProvider.validateRefreshToken(refreshToken)) {
                String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
                String newToken = jwtTokenProvider.generateToken(username);
                return ResponseEntity.ok(new JwtAuthenticationResponse(newToken, refreshToken));
            } else {
                return ResponseEntity.status(401).body(null); // 리프레시 토큰이 유효하지 않으면 401 응답
            }
        } catch (Exception e) {
            logger.error("리프레시 토큰 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(500).body(null); // 서버 오류 처리
        }
    }

}
