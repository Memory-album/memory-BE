package com.min.i.memory_BE.domain.user.controller;

import com.min.i.memory_BE.domain.user.dto.JwtAuthenticationResponse;
import com.min.i.memory_BE.domain.user.dto.UserLoginDto;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.enums.UserStatus;
import com.min.i.memory_BE.domain.user.service.UserService;
import com.min.i.memory_BE.domain.user.security.CustomUserDetails;
import com.min.i.memory_BE.global.config.SecurityConfig;
import com.min.i.memory_BE.global.security.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") //응답 헤더에 자동으로 Access-Control-Allow-Credentials: true가 포함
public class LoginController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private UserService userService;

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 실패"),
            @ApiResponse(responseCode = "423", description = "계정이 잠김")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDto loginDto) {
        try {
            // 이메일이 존재하는지 확인
            User user = userService.getUserByEmail(loginDto.getEmail());
            if (user == null) {
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

            // 실제 인증 수행
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDto.getEmail(), 
                    loginDto.getPassword()
                )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // 인증 성공 시 토큰 생성
            JwtAuthenticationResponse tokens = userService.generateTokens(loginDto.getEmail());

            // JWT 쿠키 설정
            ResponseCookie accessTokenCookie = ResponseCookie.from("jwtToken", tokens.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60 * 60) // 1시간
                .sameSite("Strict")
                .build();

            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60 * 60 * 24 * 7) // 7일
                .sameSite("Strict")
                .build();

            // 사용자 상태 확인
            if (userDetails.getStatus() == UserStatus.INACTIVE) {
                return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(Map.of(
                        "status", "warning",
                        "message", "계정이 비활성화 상태입니다. 활성화가 필요합니다.",
                        "user", Map.of(
                            "email", userDetails.getEmail(),
                            "name", userDetails.getName(),
                            "profileImgUrl", userDetails.getProfileImgUrl(),
                            "status", userDetails.getStatus()
                        )
                    ));
            }

            // 로그인 성공 시 로그인 시도 횟수 초기화
            userService.unlockAccount(loginDto.getEmail());

            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(Map.of(
                    "status", "success",
                    "message", "로그인 성공",
                    "user", Map.of(
                        "email", userDetails.getEmail(),
                        "name", userDetails.getName(),
                        "profileImgUrl", userDetails.getProfileImgUrl()
                    )
                ));

        } catch (BadCredentialsException e) {
            // 로그인 실패 시 시도 횟수 증가
            int attempts = userService.incrementLoginAttempts(loginDto.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("비밀번호가 틀렸습니다. (로그인 시도 횟수: " + attempts + "/5)");
        }
    }

    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자를 로그아웃합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "로그인되어 있지 않음")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(name = "jwtToken", required = false) String accessToken) {
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "message", "이미 로그아웃 되었거나 로그인되어 있지 않습니다.",
                    "status", "error"
                ));
        }

        // 토큰 검증
        if (!jwtTokenProvider.validateToken(accessToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "message", "유효하지 않은 토큰입니다.",
                    "status", "error"
                ));
        }

        // JWT 쿠키 삭제
        ResponseCookie deleteAccessTokenCookie = ResponseCookie.from("jwtToken", "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(0)
            .sameSite("Strict")
            .build();

        ResponseCookie deleteRefreshTokenCookie = ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(0)
            .sameSite("Strict")
            .build();

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, deleteAccessTokenCookie.toString())
            .header(HttpHeaders.SET_COOKIE, deleteRefreshTokenCookie.toString())
            .body(Map.of(
                "message", "로그아웃 되었습니다.",
                "status", "success"
            ));
    }

    @Operation(
            summary = "리프레시 토큰을 이용해 새로운 액세스 토큰 발급",
            description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "새로운 액세스 토큰 발급 성공"),
            @ApiResponse(responseCode = "401", description = "리프레시 토큰이 유효하지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = "refreshToken", required = true) String refreshToken) {
        try {
            if (jwtTokenProvider.validateRefreshToken(refreshToken)) {
                String email = jwtTokenProvider.getEmailFromToken(refreshToken);
                String newAccessToken = jwtTokenProvider.generateToken(email);

                // 새로운 액세스 토큰을 쿠키에 설정
                ResponseCookie accessTokenCookie = ResponseCookie.from("jwtToken", newAccessToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(60 * 60) // 1시간
                    .sameSite("Strict")
                    .build();

                return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                    .body(Map.of(
                        "message", "새로운 액세스 토큰이 발급되었습니다.",
                        "status", "success"
                    ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "message", "리프레시 토큰이 유효하지 않습니다.",
                        "status", "error"
                    ));
            }
        } catch (Exception e) {
            logger.error("리프레시 토큰 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "message", "토큰 갱신 중 오류가 발생했습니다.",
                    "status", "error"
                ));
        }
    }

}
