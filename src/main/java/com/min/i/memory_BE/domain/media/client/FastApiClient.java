package com.min.i.memory_BE.domain.media.client;

import com.min.i.memory_BE.global.error.exception.FastApiServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FastApiClient {

    @Value("${fastapi.server.url}")
    private String fastApiUrl;

    @Value("${google.api.key:}")
    private String googleApiKey;

    private final RestTemplate restTemplate;

    /**
     * 이미지를 FastAPI 서버로 전송하여 분석을 요청합니다.
     */
    public Map<String, Object> analyzeImage(MultipartFile image) {
        try {
            log.info("FastAPI 서버로 이미지 분석 요청: {}", fastApiUrl);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource resource = new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            };
            // FastAPI 서버의 파라미터 이름은 'image'입니다
            body.add("image", resource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                fastApiUrl + "/api/v1/analyze-image",  // 접두사 포함된 경로로 수정
                requestEntity,
                Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new FastApiServiceException("FastAPI 서버 응답 오류: " + response.getStatusCode());
            }
            
            log.info("FastAPI 서버로부터 응답 수신 성공");
            return (Map<String, Object>) response.getBody();

        } catch (Exception e) {
            log.error("이미지 분석 중 오류 발생: {}", e.getMessage(), e);
            throw new FastApiServiceException("이미지 분석 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * S3 이미지 URL을 FastAPI 서버로 전송하여 분석을 요청합니다.
     * 
     * @param imageUrl S3에 저장된 이미지 URL
     * @param authToken 인증 토큰 (선택적)
     * @return 분석 결과
     */
    public Map<String, Object> analyzeImageByUrl(String imageUrl, String authToken) {
        try {
            log.info("FastAPI 서버로 이미지 URL 분석 요청: {}, URL: {}", fastApiUrl, imageUrl);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 인증 토큰이 있는 경우 Authorization 헤더 추가
            if (authToken != null && !authToken.isEmpty()) {
                headers.setBearerAuth(authToken);
            }

            // 요청 본문 구성 - 이미지 URL 포함
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("image_url", imageUrl);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            // 올바른 엔드포인트 경로 사용 (/api/v1 접두사 포함)
            ResponseEntity<Map> response = restTemplate.postForEntity(
                fastApiUrl + "/api/v1/analyze-image-url",
                requestEntity,
                Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new FastApiServiceException("FastAPI 서버 응답 오류: " + response.getStatusCode());
            }
            
            log.info("FastAPI 서버로부터 응답 수신 성공");
            return response.getBody();

        } catch (Exception e) {
            log.error("이미지 URL 분석 중 오류 발생: {}", e.getMessage(), e);
            throw new FastApiServiceException("이미지 URL 분석 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * FastAPI 서버 URL을 반환합니다.
     * 
     * @return FastAPI 서버 URL
     */
    public String getFastApiUrl() {
        return fastApiUrl;
    }
    
    /**
     * RestTemplate 인스턴스를 반환합니다.
     * 
     * @return RestTemplate 인스턴스
     */
    public RestTemplate getRestTemplate() {
        return restTemplate;
    }
    
    /**
     * 질문-답변 데이터 + 이미지를 FastAPI 서버로 전송하여 스토리텔링을 생성합니다.
     * 
     * @param mediaId 미디어 ID
     * @param questions 질문 목록 (각 질문은 id, content, category, level, theme 등의 정보를 포함)
     * @param answers 답변 목록 (각 답변은 id, content, user_id 등의 정보를 포함)
     * @param options 스토리 생성 옵션 (스타일, 길이 등)
     * @param imageUrl 이미지 URL (Gemini 1.5 Flash 모델에 전달할 이미지 URL)
     * @return 생성된 스토리 정보를 포함한 응답
     */
    public Map<String, Object> generateStory(Long mediaId, List<Map<String, Object>> questions, 
                                         List<Map<String, Object>> answers, Map<String, Object> options,
                                         String imageUrl) {
        try {
            log.info("FastAPI 서버로 스토리 생성 요청: {}", fastApiUrl);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 요청 본문 구성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("media_id", mediaId);
            requestBody.put("questions", questions);
            requestBody.put("answers", answers);
            requestBody.put("image_url", imageUrl);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // FastAPI 서버에 요청 전송
            ResponseEntity<Map> response = restTemplate.postForEntity(
                fastApiUrl + "/api/v1/generate-story",
                requestEntity,
                Map.class
            );

            // 응답 상태 확인
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new FastApiServiceException("FastAPI 서버 응답 오류: " + response.getStatusCode());
            }
            
            log.info("FastAPI 서버로부터 스토리 생성 응답 수신 성공");
            return response.getBody();

        } catch (Exception e) {
            log.error("스토리 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new FastApiServiceException("스토리 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * 질문-답변 데이터를 FastAPI 서버로 전송하여 스토리텔링을 생성합니다. (이전 버전과의 호환성 유지)
     * 
     * @param mediaId 미디어 ID
     * @param questions 질문 목록 (각 질문은 id, content, category, level, theme 등의 정보를 포함)
     * @param answers 답변 목록 (각 답변은 id, content, user_id 등의 정보를 포함)
     * @param options 스토리 생성 옵션 (스타일, 길이 등)
     * @return 생성된 스토리 정보를 포함한 응답
     */
    public Map<String, Object> generateStory(Long mediaId, List<Map<String, Object>> questions, 
                                         List<Map<String, Object>> answers, Map<String, Object> options) {
        // 이미지 URL 없이 호출되는 경우 null로 전달
        return generateStory(mediaId, questions, answers, options, null);
    }
} 