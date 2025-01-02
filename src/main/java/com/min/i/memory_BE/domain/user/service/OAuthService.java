package com.min.i.memory_BE.domain.user.service;

import com.min.i.memory_BE.domain.user.entity.OAuthAccount;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.enums.OAuthProvider;
import com.min.i.memory_BE.domain.user.repository.OAuthAccountRepository;
import com.min.i.memory_BE.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${naver.oauth.client-id}")
    private String naverClientId;

    @Value("${naver.oauth.client-secret}")
    private String naverClientSecret;

    @Value("${naver.oauth.redirect-uri}")
    private String naverRedirectUri;

    @Value("${naver.oauth.token-url}")
    private String naverTokenUrl;

    @Value("${naver.oauth.auth-url}")
    private String naverAuthUrl;

    @Value("${kakao.oauth.client-id}")
    private String kakaoClientId;

    @Value("${kakao.oauth.client-secret}")
    private String kakaoClientSecret;

    @Value("${kakao.oauth.token-url}")
    private String kakaoTokenUrl;

    @Value("${kakao.oauth.redirect-uri}")
    private String kakaoRedirectUri;

    private final UserRepository userRepository;
    private final OAuthAccountRepository oAuthAccountRepository;

    public OAuthService(UserRepository userRepository, OAuthAccountRepository oAuthAccountRepository) {
        this.userRepository = userRepository;
        this.oAuthAccountRepository = oAuthAccountRepository;
    }

    public String generateNaverAuthUrl() {
        return naverAuthUrl +
                "?response_type=code" +
                "&client_id=" + naverClientId +
                "&redirect_uri=" + naverRedirectUri +
                "&state=" + generateState();
    }

    public String generateKakaoAuthUrl() {
        return "https://kauth.kakao.com/oauth/authorize" +
                "?response_type=code" +
                "&client_id=" + kakaoClientId +
                "&redirect_uri=" + kakaoRedirectUri;
    }


    private String generateState() {
        return UUID.randomUUID().toString();
    }

    public void handleNaverCallback(String code, String state) {

        String tokenRequestUrl = buildNaverTokenRequestUrl(code, state);
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

    public void handleKakaoCallback(String code) {
        RestTemplate restTemplate = new RestTemplate();

        // 카카오 토큰 요청 URL 생성
        String tokenRequestUrl = buildKakaoTokenRequestUrl(code);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> tokenResponse;
        try {
            tokenResponse = restTemplate.exchange(
                    tokenRequestUrl,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );
        } catch (Exception e) {
            throw new RuntimeException("카카오 액세스 토큰 요청 실패", e);
        }

        Map<String, Object> tokenResponseBody = tokenResponse.getBody();
        if (tokenResponseBody == null || !tokenResponseBody.containsKey("access_token")) {
            throw new RuntimeException("카카오 액세스 토큰이 없습니다.");
        }

        String accessToken = (String) tokenResponseBody.get("access_token");
        fetchKakaoUserProfile(accessToken);
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

    private void fetchKakaoUserProfile(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String profileUrl = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> response;
        try {
            response = restTemplate.exchange(
                    profileUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );
        } catch (Exception e) {
            throw new RuntimeException("카카오 사용자 프로필 요청 실패", e);
        }

        Map<String, Object> userProfile = response.getBody();

        if (userProfile == null) {
            throw new RuntimeException("카카오 사용자 정보가 비어 있습니다.");
        }

        saveOrUpdateKakaoUser(userProfile);
    }


    private void saveOrUpdateUser(Map<String, Object> userProfile, String accessToken) {
        // 프로필 사진, 이름 등 필요한 정보 추출
        String providerUserId = (String) userProfile.get("id"); // 네이버 사용자 고유 ID
        String name = (String) userProfile.getOrDefault("name", "네이버 사용자"); // 기본값 "사용자"
        String profileImgUrl = (String) userProfile.getOrDefault("profile_image", "default-profile-img-url"); // 기본 이미지 URL
        String email = (String) userProfile.get("email"); // 이메일

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

    private void saveOrUpdateKakaoUser(Map<String, Object> userProfile) {
        // 사용자 정보 추출
        String providerUserId = String.valueOf(userProfile.get("id")); // 카카오 사용자 고유 ID
        Map<String, Object> kakaoAccount = (Map<String, Object>) userProfile.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        String nickname = (String) profile.get("nickname"); // 닉네임
        String profileImgUrl = (String) profile.get("profile_image_url"); // 프로필 사진

        String email = "이메일 입력이 필요합니다."; // 카카오는 이메일 기본 제공 안해줌 사업자 정보있어야함..

        // 기존 OAuthAccount 확인 - OAuthProvider와 providerUserId로 OAuthAccount 검색
        Optional<OAuthAccount> existingOAuthAccount = oAuthAccountRepository.findByProviderAndProviderUserId(OAuthProvider.KAKAO, providerUserId);

        if (existingOAuthAccount.isPresent()) {
            // 기존 사용자 업데이트
            OAuthAccount oauthAccount = existingOAuthAccount.get();
            oauthAccount.updateAccessToken(providerUserId, LocalDateTime.now().plusHours(1)); // Access Token 업데이트
            oAuthAccountRepository.save(oauthAccount);

        } else {
                // 새로운 사용자 생성
                User newUser = User.builder()
                        .name(nickname) // 닉네임 저장
                        .email(email) // 이메일 저장
                        .profileImageUrl(profileImgUrl) // 프로필 이미지 저장
                        .emailVerified(false) // 이메일 없는 경우 기본값 false
                        .build();

                userRepository.save(newUser);

                // 새로운 OAuthAccount 생성
                OAuthAccount newOAuthAccount = OAuthAccount.builder()
                        .user(newUser)
                        .provider(OAuthProvider.KAKAO)
                        .providerUserId(providerUserId) // 카카오 고유 ID 저장
                        .tokenExpiresAt(LocalDateTime.now().plusHours(1))
                        .build();
                oAuthAccountRepository.save(newOAuthAccount);
        }
    }


    private String buildNaverTokenRequestUrl(String code, String state) {
        return naverTokenUrl +
                "?grant_type=authorization_code" +
                "&client_id=" + naverClientId +
                "&client_secret=" + naverClientSecret +
                "&code=" + code +
                "&state=" + state;
    }

    private String buildKakaoTokenRequestUrl(String code) {
        return kakaoTokenUrl +
                "?grant_type=authorization_code" +
                "&client_id=" + kakaoClientId +
                "&client_secret=" + kakaoClientSecret +
                "&redirect_uri=" + kakaoRedirectUri +
                "&code=" + code;
    }

}

