package com.min.i.memory_BE.domain.user.controller;

import com.min.i.memory_BE.domain.user.dto.UserRegisterDto;
import com.min.i.memory_BE.domain.user.dto.UserRegisterResultDto;
import com.min.i.memory_BE.domain.user.service.EmailService;
import com.min.i.memory_BE.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    // 이메일 인증 코드 발송
    @Operation(
            summary = "이메일 인증 코드 발송",
            description = "사용자에게 이메일 인증 코드를 전송하고, JWT 토큰을 생성하여 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "인증 코드 전송 성공",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "이메일 전송 실패"
            )
    })
    @PostMapping("/send-verification-code")
    public ResponseEntity<String> sendVerificationCode(@Parameter(description = "인증코드를 받을 이메일 주소")
                                                       @RequestBody UserRegisterDto userRegisterDto) {
        // JWT 생성 및 이메일 전송
        String jwt = emailService.sendVerificationCode(userRegisterDto);

        System.out.println("인증 코드가 이메일로 전송되었습니다. JWT: " + jwt);

        if (jwt != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .body("인증 코드가 이메일로 전송되었습니다.");
        } else {
            return ResponseEntity.status(500).body("이메일 전송에 실패했습니다.");
        }
    }

    @Operation(
            summary = "이메일 인증",
            description = "사용자가 전송된 이메일 인증 코드를 사용하여 이메일을 인증합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "이메일 인증 성공",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "이메일 인증 실패 (인증 코드 오류)"
            )
    })
    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@Parameter(description = "이메일로 받은 인증 코드")
                                              @RequestBody UserRegisterDto userRegisterDto,
                                              @Parameter(description = "Authorization 헤더에 포함된 JWT 토큰")
                                              @RequestHeader("Authorization") String authorization) {
        // JWT 토큰 추출 (Bearer 토큰에서 실제 JWT 값을 추출)
        String jwtToken = authorization.replace("Bearer ", "");

        // JWT 유효성 검사 후 이메일 인증
        String newJwt = userService.verifyEmail(jwtToken, userRegisterDto.getEmailVerificationCode());

        // JWT 출력 (서버 로그에 출력)
        System.out.println("이메일 인증에 성공했습니다. New JWT: " + newJwt);

        //인증 성공 시 새로운 JWT를 Authorization 헤더에 포함시켜 반환
        if (newJwt != null) {
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                    .body("이메일 인증 성공");
        } else {
            return ResponseEntity.status(400).body("이메일 인증에 실패했습니다. 인증 코드를 다시 확인하세요.");
        }
    }

    @Operation(
            summary = "최종 회원가입 처리",
            description = "인증된 이메일로 최종 회원가입을 완료합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 완료",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "회원가입 실패 (잘못된 JWT 토큰 또는 사용자 정보)"
            )
    })
    @PostMapping("/complete-register")
    public ResponseEntity<String> completeRegister(@Parameter(description = "회원가입에 필요한 추가 사용자 정보(비밀번호, 이름, 프로필 사진 링크)")
                                                   @RequestBody UserRegisterDto userRegisterDto,
                                                   @Parameter(description = "Authorization 헤더에 포함된 JWT 토큰")
                                                   @RequestHeader("Authorization") String authorization) {

        // JWT 토큰 추출 (Bearer 토큰에서 실제 JWT 값을 추출)
        String jwtToken = authorization.replace("Bearer ", "");

        // 인증된 이메일로 최종 회원가입 처리
        UserRegisterResultDto result = userService.completeRegister(userRegisterDto, jwtToken);

        return ResponseEntity.ok(result.getMessage());
    }

}
