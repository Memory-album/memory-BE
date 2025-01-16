package com.min.i.memory_BE.domain.user.controller;

import com.min.i.memory_BE.domain.user.dto.UserRegisterDto;
import com.min.i.memory_BE.domain.user.service.EmailService;
import com.min.i.memory_BE.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseCookie;
import java.util.Map;

@RestController
@RequestMapping("/register")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class RegisterController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Operation(
            summary = "이메일 인증 코드 발송",
            description = "사용자에게 이메일 인증 코드를 전송하고, JWT 토큰을 생성하여 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 코드 전송 성공"),
            @ApiResponse(responseCode = "500", description = "이메일 전송 실패")
    })
    @PostMapping("/send-verification-code")
    public ResponseEntity<?> sendVerificationCode(
            @Parameter(description = "인증코드를 받을 이메일 주소")
            @RequestBody UserRegisterDto userRegisterDto) {
        String jwt = emailService.sendVerificationCode(userRegisterDto);
        
        if (jwt != null) {
            ResponseCookie jwtCookie = ResponseCookie.from("verificationToken", jwt)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60 * 15) // 15분
                .sameSite("Strict")
                .build();

            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(Map.of(
                    "message", "인증 코드가 이메일로 전송되었습니다.",
                    "status", "success"
                ));
        }
        
        return ResponseEntity.status(500)
            .body(Map.of(
                "message", "이메일 전송에 실패했습니다.",
                "status", "error"
            ));
    }

    @Operation(
            summary = "이메일 인증",
            description = "사용자가 전송된 이메일 인증 코드를 사용하여 이메일을 인증합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이메일 인증 성공"),
            @ApiResponse(responseCode = "400", description = "이메일 인증 실패")
    })
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(
            @Parameter(description = "이메일 인증 정보") 
            @RequestBody UserRegisterDto userRegisterDto,
            @CookieValue(name = "verificationToken", required = true) String jwtToken) {
        try {
            String newToken = userService.verifyEmail(jwtToken, userRegisterDto.getEmailVerificationCode());
            
            if (newToken != null) {
                ResponseCookie jwtCookie = ResponseCookie.from("verificationToken", newToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(60 * 15) // 15분
                    .sameSite("Strict")
                    .build();

                return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(Map.of(
                        "message", "이메일 인증이 완료되었습니다.",
                        "status", "success"
                    ));
            }
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "message", "인증 코드가 유효하지 않습니다.",
                    "status", "error"
                ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "message", e.getMessage(),
                    "status", "error"
                ));
        }
    }

    @Operation(
            summary = "최종 회원가입 처리",
            description = "인증된 이메일로 최종 회원가입을 완료합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 완료"),
            @ApiResponse(responseCode = "400", description = "회원가입 실패")
    })
    @PostMapping("/complete-register")
    public ResponseEntity<?> completeRegister(
            @Parameter(description = "회원가입 정보") 
            @RequestBody UserRegisterDto userRegisterDto,
            @CookieValue(name = "verificationToken", required = true) String jwtToken) {
        try {
            userService.completeRegister(userRegisterDto, jwtToken);
            
            // 인증 완료 후 verificationToken 쿠키 삭제
            ResponseCookie deleteCookie = ResponseCookie.from("verificationToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(Map.of(
                    "message", "회원가입이 완료되었습니다.",
                    "status", "success"
                ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "message", e.getMessage(),
                    "status", "error"
                ));
        }
    }
}
