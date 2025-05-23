package com.min.i.memory_BE.domain.user.controller;

import com.min.i.memory_BE.domain.user.service.UserService;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.dto.UserUpdateDto;
import com.min.i.memory_BE.domain.user.dto.PasswordResetDto;
import com.min.i.memory_BE.domain.user.dto.UserResponse;
import com.min.i.memory_BE.domain.user.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(
            summary = "로그인 페이지로 이동",
            description = "사용자를 로그인 페이지로 리디렉션합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "로그인 페이지로 이동"
    )
    @GetMapping("/loginPage")
    public ResponseEntity<String> loginPage() {
        return ResponseEntity.ok("로그인 페이지로 이동");
    }
//

    @Operation(
            summary = "홈 페이지로 이동",
            description = "로그인된 사용자의 홈 페이지 정보를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "홈 페이지 정보 반환 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요")
    })
    @GetMapping("/home")
    public ResponseEntity<?> homePage(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "message", "로그인이 필요합니다.",
                    "status", "error"
                ));
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok()
            .body(Map.of(
                "status", "success",
                "user", Map.of(
                    "email", userDetails.getEmail(),
                    "name", userDetails.getName(),
                    "profileImgUrl", userDetails.getProfileImgUrl()
                )
            ));
    }
//

    @Operation(
            summary = "마이 페이지로 이동",
            description = "로그인된 사용자의 상세 정보를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 정보 반환 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요")
    })
    @GetMapping("/my-page")
    public ResponseEntity<?> myPage(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "message", "로그인이 필요합니다.",
                    "status", "error"
                ));
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok()
            .body(Map.of(
                "status", "success",
                "user", Map.of(
                    "id", userDetails.getId(),
                    "email", userDetails.getEmail(),
                    "name", userDetails.getName(),
                    "profileImgUrl", userDetails.getProfileImgUrl(),
                    "status", userDetails.getStatus()
                )
            ));
    }
    
    @Operation(
            summary = "사용자 정보 수정",
            description = "로그인된 사용자의 정보(이름, 비밀번호, 프로필 이미지)를 수정합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정보 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateUser(
            @RequestPart(value = "userUpdateDto") String userUpdateDtoJson,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            Authentication authentication) {
        try {
            // JSON 문자열을 DTO로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            UserUpdateDto updateDto = objectMapper.readValue(userUpdateDtoJson, UserUpdateDto.class);
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "message", "로그인이 필요합니다.",
                        "status", "error"
                    ));
            }

            String email = authentication.getName();
            User updatedUser = userService.updateUser(email, updateDto, profileImage);

            return ResponseEntity.ok()
                .body(Map.of(
                    "message", "사용자 정보가 성공적으로 수정되었습니다.",
                    "status", "success",
                    "user", Map.of(
                        "email", updatedUser.getEmail(),
                        "name", updatedUser.getName(),
                        "profileImgUrl", updatedUser.getProfileImgUrl()
                    )
                ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "message", e.getMessage(),
                    "status", "error"
                ));
        }
    }

    // 사용자 탈퇴 (계정 영구 삭제)
    @Operation(
            summary = "사용자 탈퇴",
            description = "계정을 영구적으로 삭제합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "탈퇴 처리 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "자신의 계정만 탈퇴할 수 있습니다")
    })
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(Authentication authentication) {
        // 인증 확인
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "message", "로그인이 필요합니다.",
                    "status", "error"
                ));
        }

        try {
            String email = authentication.getName();
            userService.deleteUser(email);

            // 로그아웃 처리 (쿠키 삭제)
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
                    "message", "회원 탈퇴가 완료되었습니다.",
                    "status", "success"
                ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "message", e.getMessage(),
                    "status", "error"
                ));
        }
    }

    // 사용자 계정 비활성화
    @Operation(
            summary = "계정 비활성화",
            description = "사용자 계정을 비활성화 상태로 변경합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "계정 비활성화 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "자신의 계정만 비활성화할 수 있습니다")
    })
    @PostMapping("/deactivate")
    public ResponseEntity<?> deactivateAccount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "message", "로그인이 필요합니다.",
                    "status", "error"
                ));
        }

        try {
            String email = authentication.getName();
            userService.deactivateUser(email);

            // 로그아웃 처리 (쿠키 삭제)
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
                    "message", "계정이 비활성화되었습니다.",
                    "status", "success"
                ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "message", e.getMessage(),
                    "status", "error"
                ));
        }
    }

    // 사용자 계정 활성화
    @Operation(
            summary = "계정 활성화",
            description = "비활성화된 계정을 다시 활성화합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "계정 활성화 성공"),
            @ApiResponse(responseCode = "400", description = "이미 활성화된 계정입니다"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "자신의 계정만 활성화할 수 있습니다")
    })
    @PostMapping("/activate")
    public ResponseEntity<?> activateAccount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "message", "로그인이 필요합니다.",
                    "status", "error"
                ));
        }

        try {
            String email = authentication.getName();
            userService.activateUser(email);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "message", "계정이 활성화되었습니다.",
                    "status", "success"
                ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "message", e.getMessage(),
                    "status", "error"
                ));
        }
    }

    @Operation(
            summary = "비밀번호 재설정 요청",
            description = "사용자의 이메일로 비밀번호 재설정 코드를 전송합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 재설정 이메일 전송 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (존재하지 않는 이메일)"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류"
            )
    })
    @PostMapping("/password/reset-request")
    public ResponseEntity<?> requestPasswordReset(
            @Parameter(description = "사용자 이메일", required = true)
            @RequestBody PasswordResetDto request) {
        try {
            // JWT 토큰을 받아옴
            String jwt = userService.requestPasswordReset(request.getEmail());
            
            ResponseCookie resetCookie = ResponseCookie.from("passwordResetToken", jwt)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60 * 15) // 15분
                .sameSite("Strict")
                .build();
            
            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resetCookie.toString())
                .body(Map.of(
                    "message", "비밀번호 재설정 이메일이 전송되었습니다.",
                    "status", "success"
                ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", e.getMessage(),
                "status", "error"
            ));
        }
    }

    @Operation(
            summary = "비밀번호 재설정 코드 검증",
            description = "이메일로 전송된 비밀번호 재설정 코드를 검증합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "코드 검증 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 코드"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PostMapping("/password/verify-code")
    public ResponseEntity<?> verifyPasswordResetCode(
            @Parameter(description = "이메일, 코드 정보", required = true)
            @RequestBody PasswordResetDto request,
            @CookieValue(name = "passwordResetToken", required = true) String jwtToken) {
        try {
            boolean isValid = userService.verifyPasswordResetCode(
                request.getEmail(), 
                request.getVerificationCode(), 
                jwtToken
            );
            
            if (isValid) {
                ResponseCookie resetCookie = ResponseCookie.from("passwordResetToken", jwtToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(60 * 15) // 15분
                    .sameSite("Strict")
                    .build();

                return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, resetCookie.toString())
                    .body(Map.of(
                        "message", "인증이 완료되었습니다. 새로운 비밀번호를 입력해주세요.",
                        "status", "success"
                    ));
            }
            return ResponseEntity.badRequest().body(Map.of(
                "message", "잘못된 인증번호입니다.",
                "status", "error"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", e.getMessage(),
                "status", "error"
            ));
        }
    }

    @Operation(
            summary = "새 비밀번호 설정",
            description = "검증된 코드로 새 비밀번호를 설정합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PutMapping("/password/reset")
    public ResponseEntity<?> resetPassword(
            @RequestBody PasswordResetDto request,
            @CookieValue(name = "passwordResetToken", required = true) String jwtToken) {
        try {
            if (request.getEmail() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "이메일 정보가 필요합니다.",
                    "status", "error"
                ));
            }
            
            userService.resetPassword(request, jwtToken);
            
            // 비밀번호 재설정 후 쿠키 삭제
            ResponseCookie deleteCookie = ResponseCookie.from("passwordResetToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
                
            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(Map.of(
                    "message", "비밀번호가 성공적으로 변경되었습니다.",
                    "status", "success"
                ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", e.getMessage(),
                "status", "error"
            ));
        }
    }

    @Operation(
            summary = "ID로 사용자 조회",
            description = "사용자 ID로 사용자 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok()
                    .body(Map.of(
                            "status", "success",
                            "user", UserResponse.from(user)
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message", e.getMessage(),
                            "status", "error"
                    ));
        }
    }
}
