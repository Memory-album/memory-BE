package com.min.i.memory_BE.domain.user.controller;

import com.min.i.memory_BE.domain.user.enums.OAuthProvider;
import com.min.i.memory_BE.domain.user.service.OAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/oauth")
public class OAuthController {

    private final OAuthService oAuthService;

    public OAuthController(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    @Operation(
            summary = "OAuth 로그인 리디렉션",
            description = "사용자를 선택된 OAuth 제공자의 로그인 페이지로 리디렉션합니다."
    )
    @ApiResponse(
            responseCode = "302",
            description = "로그인 페이지로 리디렉션 성공",
            content = @Content(mediaType = "application/json")
    )
    @GetMapping("/login")
    public ResponseEntity<Void> redirectToLogin(@Parameter(description = "OAuth 제공자 이름 (예: google, kakao, naver)")
                                                @RequestParam("provider") String providerName) {
        OAuthProvider provider = OAuthProvider.valueOf(providerName.toUpperCase());
        String authUrl = oAuthService.generateAuthUrl(provider);
        return ResponseEntity.status(HttpStatus.FOUND).header("Location", authUrl).build();
    }

    @Operation(
            summary = "OAuth 로그인 콜백 처리",
            description = "OAuth 제공자로부터 받은 콜백을 처리하고 로그인 성공 메시지를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (잘못된 인증 코드 또는 상태)"
            )
    })
    @GetMapping("/callback")
    public ResponseEntity<String> handleCallback(@Parameter(description = "OAuth 제공자 이름 (예: google, kakao, naver)")
                                                 @RequestParam("provider") String providerName,
                                                 @Parameter(description = "OAuth 제공자가 반환한 인증 코드")
                                                 @RequestParam("code") String code,
                                                 @Parameter(description = "상태 값 (선택 사항)")
                                                 @RequestParam(value = "state", required = false) String state) {
        OAuthProvider provider = OAuthProvider.valueOf(providerName.toUpperCase());
        oAuthService.handleCallback(provider, code, state);
        return ResponseEntity.ok(providerName + " 로그인 성공");
    }

    @Operation(
            summary = "OAuth 로그아웃",
            description = "OAuth 제공자에서 로그아웃을 처리하고 세션을 무효화합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (잘못된 제공자 이름)"
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @Parameter(description = "로그아웃할 OAuth 제공자 이름 (예: google, kakao, naver)")
            @RequestParam("provider") String provider,
            @Parameter(description = "OAuth 제공자에서 발급된 액세스 토큰")
            @RequestParam("token") String accessToken,
            @Parameter(description = "HTTP 요청 객체")
            HttpServletRequest request) {

        // 서버 세션 무효화
        request.getSession().invalidate();

        // OAuth 제공자별 로그아웃 처리
        switch (provider.toLowerCase()) {
            case "google":
                logoutFromGoogle(accessToken);
                break;
            case "kakao":
                logoutFromKakao(accessToken);
                break;
            case "naver":
                logoutFromNaver(); // 네이버는 서버 세션 무효화만 처리
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 제공자: " + provider);
        }

        // 로그아웃 완료 메시지만 반환하고 리다이렉트는 프론트에서 처리
        return ResponseEntity.ok("로그아웃 완료");
    }

    private void logoutFromGoogle(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String revokeUrl = "https://accounts.google.com/o/oauth2/revoke?token=" + accessToken;

        ResponseEntity<String> response = restTemplate.exchange(
                revokeUrl,
                HttpMethod.GET,
                null,
                String.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to revoke Google token");
        }
    }

    private void logoutFromKakao(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);
        String logoutUrl = "https://kapi.kakao.com/v1/user/logout";

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                logoutUrl,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {
                }
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to logout from Kakao");
        }
    }

    private void logoutFromNaver() {
        // 네이버는 별도의 로그아웃 API가 없음
        // 이미 위에서 request.getSession().invalidate()로 처리됨
        System.out.println("Naver session 무효화됨");
    }

}

