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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class OAuthService {

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String naverClientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;

    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    private String naverRedirectUri;

    @Value("${spring.security.oauth2.client.provider.naver.token-uri}")
    private String naverTokenUrl;

    @Value("${spring.security.oauth2.client.provider.naver.authorization-uri}")
    private String naverAuthUrl;

    @Value("${kakao.oauth.client-id}")
    private String kakaoClientId;

    @Value("${kakao.oauth.client-secret}")
    private String kakaoClientSecret;

    @Value("${kakao.oauth.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${kakao.oauth.token-url}")
    private String kakaoTokenUrl;

    @Value("${kakao.oauth.auth-url}")
    private String kakaoAuthUrl;

    //

    @Value("${google.oauth.client-id}")
    private String googleClientId;

    @Value("${google.oauth.client-secret}")
    private String googleClientSecret;

    @Value("${google.oauth.redirect-uri}")
    private String googleRedirectUri;

    @Value("${google.oauth.token-url}")
    private String googleTokenUrl;

    @Value("${google.oauth.auth-url}")
    private String googleAuthUrl;

    private final UserRepository userRepository;
    private final OAuthAccountRepository oAuthAccountRepository;

    public OAuthService(UserRepository userRepository, OAuthAccountRepository oAuthAccountRepository) {
        this.userRepository = userRepository;
        this.oAuthAccountRepository = oAuthAccountRepository;
    }

    public String generateAuthUrl(OAuthProvider provider) {
        switch (provider) {
            case NAVER:
                return naverAuthUrl +
                        "?response_type=code" +
                        "&client_id=" + naverClientId +
                        "&redirect_uri=" + naverRedirectUri +
                        "&state=" + generateState();
            case KAKAO:
                return kakaoAuthUrl +
                        "?response_type=code" +
                        "&client_id=" + kakaoClientId +
                        "&redirect_uri=" + kakaoRedirectUri;
            case GOOGLE:
                return googleAuthUrl +
                        "?client_id=" + googleClientId +
                        "&redirect_uri=" + googleRedirectUri +
                        "&response_type=code" +
                        "&scope=https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile" +
                        "&state=" + generateState() +
                        "&access_type=offline" +
                        "&prompt=consent";
            default:
                throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
        }
    }

    private String generateState() {
        return UUID.randomUUID().toString();
    }

    public String handleCallback(OAuthProvider provider, String code, String state) {
        String accessToken;
        Map<String, Object> userProfile;
        
        switch (provider) {
            case NAVER:
                accessToken = fetchAccessTokenForNaver(code, state);
                userProfile = fetchUserProfileForNaver(accessToken);
                break;
            case KAKAO:
                accessToken = fetchAccessTokenForKakao(code);
                userProfile = fetchUserProfileForKakao(accessToken);
                break;
            case GOOGLE:
                accessToken = fetchAccessTokenForGoogle(code);
                userProfile = fetchUserProfileForGoogle(accessToken);
                break;
            default:
                throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
        }
        
        // 사용자 정보 저장 또는 업데이트
        User user = saveOrUpdateUser(userProfile, provider, accessToken);
        
        // 사용자 이메일 반환
        return user.getEmail();
    }

    private String fetchAccessTokenForNaver(String code, String state) {
        String tokenRequestUrl = naverTokenUrl +
                "?grant_type=authorization_code" +
                "&client_id=" + naverClientId +
                "&client_secret=" + naverClientSecret +
                "&code=" + code +
                "&state=" + state;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                tokenRequestUrl,
                HttpMethod.POST,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("access_token")) {
            throw new RuntimeException("Failed to fetch access token for Naver");
        }

        return (String) responseBody.get("access_token");
    }

    private String fetchAccessTokenForKakao(String code) {
        String tokenRequestUrl = kakaoTokenUrl +
                "?grant_type=authorization_code" +
                "&client_id=" + kakaoClientId +
                "&client_secret=" + kakaoClientSecret +
                "&redirect_uri=" + kakaoRedirectUri +
                "&code=" + code;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                tokenRequestUrl,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("access_token")) {
            throw new RuntimeException("Failed to fetch access token for Kakao");
        }

        return (String) responseBody.get("access_token");
    }

    private String fetchAccessTokenForGoogle(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                googleTokenUrl,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {
                }
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("access_token")) {
            throw new RuntimeException("Failed to fetch access token for Google");
        }

        return (String) responseBody.get("access_token");
    }

    private Map<String, Object> fetchUserProfileForNaver(String accessToken) {
        String profileUrl = "https://openapi.naver.com/v1/nid/me";
        return fetchUserProfile(accessToken, profileUrl);
    }

    private Map<String, Object> fetchUserProfileForKakao(String accessToken) {
        String profileUrl = "https://kapi.kakao.com/v2/user/me";
        return fetchUserProfile(accessToken, profileUrl);
    }

    private Map<String, Object> fetchUserProfileForGoogle(String accessToken) {
        String profileUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
        return fetchUserProfile(accessToken, profileUrl);
    }

    private Map<String, Object> fetchUserProfile(String accessToken, String profileUrl) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                profileUrl,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );

        Map<String, Object> userProfile = response.getBody();
        if (userProfile == null) {
            throw new RuntimeException("Failed to fetch user profile");
        }
        
        return userProfile;
    }

    private User saveOrUpdateUser(Map<String, Object> userProfile, OAuthProvider provider, String accessToken) {
        String providerUserId;
        String email = null;
        String name;
        String profileImgUrl = null;

        switch (provider) {
            case NAVER:
                Object responseObj = userProfile.get("response");
                if (!(responseObj instanceof Map)) {
                    throw new RuntimeException("Invalid response format from Naver");
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> naverResponse = (Map<String, Object>) responseObj;
                providerUserId = (String) naverResponse.get("id");
                name = (String) naverResponse.getOrDefault("name", "Naver User");
                email = (String) naverResponse.get("email"); // 이메일 명시적으로 가져오기
                profileImgUrl = (String) naverResponse.getOrDefault("profile_image", "default-profile-img-url");
                break;
            case KAKAO:
                providerUserId = String.valueOf(userProfile.get("id"));
                Object kakaoAccountObj = userProfile.get("kakao_account");
                if (!(kakaoAccountObj instanceof Map)) {
                    throw new RuntimeException("Invalid kakao_account format from Kakao");
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoAccountObj;
                
                Object profileObj = kakaoAccount.get("profile");
                if (!(profileObj instanceof Map)) {
                    throw new RuntimeException("Invalid profile format from Kakao");
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> profile = (Map<String, Object>) profileObj;
                name = (String) profile.get("nickname");
                profileImgUrl = (String) profile.get("profile_image_url");
                email = "Email not provided";
                break;
            case GOOGLE:
                providerUserId = (String) userProfile.get("sub");
                email = (String) userProfile.get("email");
                name = (String) userProfile.getOrDefault("name", "Google User");
                profileImgUrl = (String) userProfile.getOrDefault("picture", "default-profile-img-url");
                break;
            default:
                throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
        }

        Optional<OAuthAccount> existingOAuthAccount = oAuthAccountRepository.findByProviderAndProviderUserId(provider, providerUserId);
        User user;

        if (existingOAuthAccount.isPresent()) {
            OAuthAccount oauthAccount = existingOAuthAccount.get();
            oauthAccount.updateAccessToken(accessToken, LocalDateTime.now().plusHours(1));
            oAuthAccountRepository.save(oauthAccount);
            user = oauthAccount.getUser();
            System.out.println("기존 사용자 조회 성공: " + user.getEmail());
        } else {
            final String finalEmail = email;
            final String finalName = name;
            final String finalProfileImgUrl = profileImgUrl;

            // 이메일로 기존 사용자 찾기
            Optional<User> existingUser = userRepository.findByEmail(finalEmail);
            
            if (existingUser.isPresent()) {
                user = existingUser.get();
                System.out.println("이메일로 기존 사용자 찾음: " + user.getEmail());
            } else {
                // 새 사용자 생성
                user = User.builder()
                        .email(finalEmail)
                        .name(finalName)
                        .profileImgUrl(finalProfileImgUrl)
                        .emailVerified(true) // OAuth 로그인의 경우 이메일이 이미 인증됨
                        .build();
                user = userRepository.save(user);
                System.out.println("새 사용자 생성 완료: " + user.getEmail());
            }

            // OAuth 계정 정보 저장
            OAuthAccount newOAuthAccount = OAuthAccount.builder()
                    .user(user)
                    .provider(provider)
                    .providerUserId(providerUserId)
                    .accessToken(accessToken)
                    .tokenExpiresAt(LocalDateTime.now().plusHours(1))
                    .build();
            oAuthAccountRepository.save(newOAuthAccount);
            System.out.println("OAuth 계정 정보 저장 완료: " + provider + ", " + providerUserId);
        }
        
        // 최종 확인
        final String userEmail = user.getEmail();
        User savedUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자 저장 실패: " + userEmail));
        System.out.println("최종 사용자 확인: " + savedUser.getEmail());
        
        return savedUser;
    }
}

