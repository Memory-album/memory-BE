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

    //

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

    public void handleCallback(OAuthProvider provider, String code, String state) {
        String accessToken;
        switch (provider) {
            case NAVER:
                accessToken = fetchAccessTokenForNaver(code, state);
                fetchUserProfileForNaver(accessToken);
                break;
            case KAKAO:
                accessToken = fetchAccessTokenForKakao(code);
                fetchUserProfileForKakao(accessToken);
                break;
            case GOOGLE:
                accessToken = fetchAccessTokenForGoogle(code);
                fetchUserProfileForGoogle(accessToken);
                break;
            default:
                throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
        }
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
                new ParameterizedTypeReference<>() {}
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
                new ParameterizedTypeReference<>() {}
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
                new ParameterizedTypeReference<>() {}
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("access_token")) {
            throw new RuntimeException("Failed to fetch access token for Google");
        }

        return (String) responseBody.get("access_token");
    }

    private void fetchUserProfileForNaver(String accessToken) {
        String profileUrl = "https://openapi.naver.com/v1/nid/me";
        fetchAndSaveUserProfile(accessToken, profileUrl, OAuthProvider.NAVER);
    }

    private void fetchUserProfileForKakao(String accessToken) {
        String profileUrl = "https://kapi.kakao.com/v2/user/me";
        fetchAndSaveUserProfile(accessToken, profileUrl, OAuthProvider.KAKAO);
    }

    private void fetchUserProfileForGoogle(String accessToken) {
        String profileUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
        fetchAndSaveUserProfile(accessToken, profileUrl, OAuthProvider.GOOGLE);
    }

    private void fetchAndSaveUserProfile(String accessToken, String profileUrl, OAuthProvider provider) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                profileUrl,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        Map<String, Object> userProfile = response.getBody();
        if (userProfile == null) {
            throw new RuntimeException("Failed to fetch user profile for " + provider);
        }

        saveOrUpdateUser(userProfile, provider, accessToken);
    }
    private void saveOrUpdateUser(Map<String, Object> userProfile, OAuthProvider provider, String accessToken) {
        String providerUserId;
        String email = null;
        String name;
        String profileImgUrl = null;

        switch (provider) {
            case NAVER:
                Map<String, Object> naverResponse = (Map<String, Object>) userProfile.get("response");
                providerUserId = (String) naverResponse.get("id");
                name = (String) naverResponse.getOrDefault("name", "Naver User");
                email = (String) naverResponse.get("email"); // 이메일 명시적으로 가져오기
                profileImgUrl = (String) naverResponse.getOrDefault("profile_image", "default-profile-img-url");
                break;
            case KAKAO:
                providerUserId = String.valueOf(userProfile.get("id"));
                Map<String, Object> kakaoAccount = (Map<String, Object>) userProfile.get("kakao_account");
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
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

        if (existingOAuthAccount.isPresent()) {
            OAuthAccount oauthAccount = existingOAuthAccount.get();
            oauthAccount.updateAccessToken(accessToken, LocalDateTime.now().plusHours(1));
            oAuthAccountRepository.save(oauthAccount);
        } else {
            final String finalEmail = email; // final 선언
            final String finalName = name;
            final String finalProfileImgUrl = profileImgUrl;

            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = User.builder()
                        .email(finalEmail)
                        .name(finalName)
                        .profileImageUrl(finalProfileImgUrl)
                        .emailVerified(finalEmail  != null && !finalEmail .isEmpty())
                        .build();
                return userRepository.save(newUser);
            });

            OAuthAccount newOAuthAccount = OAuthAccount.builder()
                    .user(user)
                    .provider(provider)
                    .providerUserId(providerUserId)
                    .accessToken(accessToken)
                    .tokenExpiresAt(LocalDateTime.now().plusHours(1))
                    .build();
            oAuthAccountRepository.save(newOAuthAccount);
        }
    }
}

