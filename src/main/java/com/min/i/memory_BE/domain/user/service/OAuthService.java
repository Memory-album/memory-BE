package com.min.i.memory_BE.domain.user.service;

import com.min.i.memory_BE.domain.user.entity.OAuthAccount;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.enums.OAuthProvider;
import com.min.i.memory_BE.domain.user.repository.OAuthAccountRepository;
import com.min.i.memory_BE.domain.user.repository.UserRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class OAuthService {

    private final String NAVER_CLIENT_ID = "YOUR_NAVER_CLIENT_ID";
    private final String NAVER_CLIENT_SECRET = "YOUR_NAVER_CLIENT_SECRET";
    private final String NAVER_REDIRECT_URI = "http://localhost:8080/api/auth/callback/naver";
    private final String NAVER_TOKEN_URL = "https://nid.naver.com/oauth2.0/token";

    private final UserRepository userRepository;
    private final OAuthAccountRepository oAuthAccountRepository;

    public OAuthService(UserRepository userRepository, OAuthAccountRepository oAuthAccountRepository) {
        this.userRepository = userRepository;
        this.oAuthAccountRepository = oAuthAccountRepository;
    }


    public String generateNaverAuthUrl() {
        return "https://nid.naver.com/oauth2.0/authorize" +
                "?response_type=code" +
                "&client_id=" + NAVER_CLIENT_ID +
                "&redirect_uri=" + NAVER_REDIRECT_URI +
                "&state=" + generateState();
    }

    private String generateState() {
        return UUID.randomUUID().toString();
    }

    public void handleNaverCallback(String code, String state) {

        String tokenRequestUrl = buildTokenRequestUrl(code, state);
        // 네이버 서버에 Access Token 요청
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map<String, Object>> response;
        try {
            response = restTemplate.exchange(
                    tokenRequestUrl,
                    HttpMethod.POST,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
        } catch (Exception e) {
            throw new RuntimeException("네이버 액세스 토큰 요청 실패", e);
        }

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("access_token")) {
            throw new RuntimeException("네이버 액세스 토큰이 없습니다.");
        }

        String accessToken = (String) responseBody.get("access_token");
        fetchUserProfileFromNaver(accessToken);
    }

    private void fetchUserProfileFromNaver(String accessToken) {
        // 네이버 사용자 프로필 요청
        RestTemplate restTemplate = new RestTemplate();
        String profileUrl = "https://openapi.naver.com/v1/nid/me";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> response;
        try {
            response = restTemplate.exchange(
                    profileUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
        } catch (Exception e) {
            throw new RuntimeException("네이버 사용자 프로필 요청에 실패했습니다.", e);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> userProfile = (Map<String, Object>) response.getBody().get("response");

        if (userProfile == null) {
            throw new RuntimeException("네이버 사용자 정보가 비어 있습니다.");
        }

        // 사용자 프로필 정보 저장
        saveOrUpdateUser(userProfile, accessToken);
    }

    private void saveOrUpdateUser(Map<String, Object> userProfile, String accessToken) {
        // 프로필 사진, 이름 등 필요한 정보 추출
        String providerUserId = (String) userProfile.get("id"); // 네이버 사용자 고유 ID
        String name = (String) userProfile.getOrDefault("name", "사용자"); // 기본값 "사용자"
        String profileImgUrl = (String) userProfile.getOrDefault("profile_image", "default-profile-img-url"); // 기본 이미지 URL

        // 이메일 정보 생성: 네이버 고유 ID를 기반으로 이메일 생성
        String email = providerUserId + "@naver.com";

        // 기존 사용자 확인
        Optional<OAuthAccount> existingOAuthAccount = oAuthAccountRepository.findByProviderAndProviderUserId(OAuthProvider.NAVER, providerUserId);

        if (existingOAuthAccount.isPresent()) {
            // 이미 등록된 OAuthAccount의 사용자 정보 업데이트
            OAuthAccount oauthAccount = existingOAuthAccount.get();
            oauthAccount.updateAccessToken(accessToken, LocalDateTime.now().plusHours(1)); // Access Token 업데이트
            oAuthAccountRepository.save(oauthAccount);
        } else {
            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = User.builder()
                        .email(email)
                        .name(name)
                        .profileImageUrl(profileImgUrl)
                        .emailVerified(true)
                        .build();
                return userRepository.save(newUser);
            });


            // 새로운 OAuthAccount 저장
            OAuthAccount newOAuthAccount = OAuthAccount.builder()
                    .user(user)
                    .provider(OAuthProvider.NAVER)
                    .providerUserId(providerUserId)
                    .accessToken(accessToken)
                    .refreshToken(null) // 필요한 경우 refreshToken 처리
                    .tokenExpiresAt(LocalDateTime.now().plusHours(1))
                    .build();
            oAuthAccountRepository.save(newOAuthAccount);
        }
    }

    private String buildTokenRequestUrl(String code, String state) {
        return NAVER_TOKEN_URL +
                "?grant_type=authorization_code" +
                "&client_id=" + NAVER_CLIENT_ID +
                "&client_secret=" + NAVER_CLIENT_SECRET +
                "&code=" + code +
                "&state=" + state;
    }


}

