package com.min.i.memory_BE.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.min.i.memory_BE.domain.user.dto.UserRegisterDto;
import com.min.i.memory_BE.domain.user.service.EmailService;
import com.min.i.memory_BE.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseCookie;

import jakarta.validation.Valid;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class RegisterController {
  
  private final UserService userService;
  private final EmailService emailService;
  
  @Operation(summary = "이메일 인증 코드 발송")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "인증 코드 전송 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
    @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping("/send-verification-code")
  public ResponseEntity<?> sendVerificationCode(
    @Parameter(description = "사용자 정보", required = true)
    @Valid @RequestBody UserRegisterDto userRegisterDto) {
    try {
      // 이메일 중복 체크
      if (userService.getUserByEmail(userRegisterDto.getEmail()) != null) {
        return ResponseEntity.badRequest()
          .body(Map.of(
            "message", "이미 가입된 이메일입니다.",
            "status", "error"
          ));
      }
      
      String jwt = emailService.sendVerificationCode(userRegisterDto);
      if (jwt == null) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of(
            "message", "인증 코드 발송에 실패했습니다.",
            "status", "error"
          ));
      }
      
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
      
    } catch (Exception e) {
      log.error("이메일 인증 코드 발송 실패", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of(
          "message", "서버 오류가 발생했습니다.",
          "status", "error"
        ));
    }
  }
  
  @Operation(summary = "이메일 인증 코드 확인")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "인증 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 인증 코드"),
    @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping("/verify-email")
  public ResponseEntity<?> verifyEmail(
    @Parameter(description = "인증 코드 정보", required = true)
    @Valid @RequestBody UserRegisterDto userRegisterDto,
    @CookieValue(name = "verificationToken", required = true) String jwtToken) {
    try {
      String newToken = userService.verifyEmail(jwtToken, userRegisterDto.getEmailVerificationCode());
      if (newToken == null) {
        return ResponseEntity.badRequest()
          .body(Map.of(
            "message", "잘못된 인증 코드입니다.",
            "status", "error"
          ));
      }
      
      ResponseCookie jwtCookie = ResponseCookie.from("verificationToken", newToken)
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(60 * 15)
        .sameSite("Strict")
        .build();
      
      return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
        .body(Map.of(
          "message", "이메일 인증이 완료되었습니다.",
          "status", "success"
        ));
      
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest()
        .body(Map.of(
          "message", e.getMessage(),
          "status", "error"
        ));
    } catch (Exception e) {
      log.error("이메일 인증 실패", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of(
          "message", "서버 오류가 발생했습니다.",
          "status", "error"
        ));
    }
  }
  
  @Operation(summary = "회원가입 완료")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "회원가입 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
    @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping(value = "/complete-register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> completeRegister(
    @Parameter(description = "사용자 정보", required = true)
    @RequestPart("userRegisterDto") String userRegisterDtoJson,
    @Parameter(description = "프로필 이미지")
    @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
    @CookieValue(name = "verificationToken", required = true) String jwtToken) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      UserRegisterDto userRegisterDto = objectMapper.readValue(userRegisterDtoJson, UserRegisterDto.class);
      
      userService.completeRegister(userRegisterDto, profileImage, jwtToken);
      
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
      
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest()
        .body(Map.of(
          "message", e.getMessage(),
          "status", "error"
        ));
    } catch (Exception e) {
      log.error("회원가입 처리 중 오류 발생", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of(
          "message", "서버 오류가 발생했습니다.",
          "status", "error",
          "details", e.getMessage()
        ));
    }
  }
  
}