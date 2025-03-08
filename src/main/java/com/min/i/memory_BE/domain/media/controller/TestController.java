package com.min.i.memory_BE.domain.media.controller;

import com.min.i.memory_BE.domain.media.client.FastApiClient;
import com.min.i.memory_BE.global.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
@Tag(name = "테스트 API", description = "서버 연결 테스트 관련 API")
public class TestController {

    private final FastApiClient fastApiClient;

    @Operation(summary = "S3 URL 기반 이미지 분석 테스트")
    @PostMapping("/analyze-s3-url")
    public ResponseEntity<?> testAnalyzeS3Url(@RequestBody Map<String, String> request) {
        try {
            String imageUrl = request.get("image_url");
            if (imageUrl == null || imageUrl.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "이미지 URL이 제공되지 않았습니다."
                ));
            }
            
            log.info("S3 URL 기반 이미지 분석 테스트 시작: {}", imageUrl);
            
            // S3 프로토콜 URL을 객체 URL로 변환 (필요한 경우)
            if (imageUrl.startsWith("s3://")) {
                imageUrl = convertS3ProtocolUrlToObjectUrl(imageUrl);
                log.info("S3 프로토콜 URL을 객체 URL로 변환: {}", imageUrl);
            }
            
            // FastAPI로 이미지 URL 전송
            Map<String, Object> analysisResult = fastApiClient.analyzeImageByUrl(imageUrl, null);
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "S3 URL 기반 이미지 분석 테스트 성공",
                    "image_url", imageUrl,
                    "analysis_result", analysisResult
            ));
            
        } catch (Exception e) {
            log.error("S3 URL 기반 이미지 분석 테스트 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", "S3 URL 기반 이미지 분석 테스트 실패: " + e.getMessage()
            ));
        }
    }
    
    @Operation(summary = "FastAPI 서버 연결 테스트")
    @GetMapping("/fastapi-connection")
    public ResponseEntity<?> testFastApiConnection(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            log.info("FastAPI 서버 연결 테스트 시작");
            
            // 인증 토큰 추출 (Bearer 접두사 제거)
            String authToken = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authToken = authHeader.substring(7);
            }
            
            // 테스트용 객체 URL 사용 (실제 존재하는 이미지 URL)
            String testUrl = "https://mini-album-storage.s3.ap-northeast-2.amazonaws.com/users/ruggy245%40naver.com/profile/a315a095-b4d1-4715-a76d-c7317ccec9d7-%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA+2025-02-26+%E1%84%8B%E1%85%A9%E1%84%8C%E1%85%A5%E1%86%AB+3.32.27.png";
            
            // FastAPI 서버로 테스트 요청 전송
            Map<String, Object> response = fastApiClient.analyzeImageByUrl(testUrl, authToken);
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "FastAPI 서버 연결 테스트 성공",
                    "response", response
            ));
            
        } catch (Exception e) {
            log.error("FastAPI 서버 연결 테스트 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", "FastAPI 서버 연결 테스트 실패: " + e.getMessage()
            ));
        }
    }
    
    @Operation(summary = "FastAPI 서버 상태 확인")
    @GetMapping("/fastapi-health")
    public ResponseEntity<?> checkFastApiHealth() {
        try {
            log.info("FastAPI 서버 상태 확인 시작");
            
            // FastAPI의 health-check 엔드포인트 호출
            String healthCheckUrl = fastApiClient.getFastApiUrl() + "/api/v1/health-check";
            
            ResponseEntity<Map> response = fastApiClient.getRestTemplate().getForEntity(
                healthCheckUrl,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "FastAPI 서버가 정상적으로 동작 중입니다",
                        "fastapi_status", response.getBody()
                ));
            } else {
                return ResponseEntity.status(response.getStatusCode()).body(Map.of(
                        "status", "error",
                        "message", "FastAPI 서버 상태 확인 실패",
                        "fastapi_status", response.getBody()
                ));
            }
            
        } catch (Exception e) {
            log.error("FastAPI 서버 상태 확인 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", "FastAPI 서버 상태 확인 실패: " + e.getMessage()
            ));
        }
    }
    
    /**
     * S3 프로토콜 URL을 객체 URL로 변환합니다.
     * 예: s3://bucket-name/key -> https://bucket-name.s3.region.amazonaws.com/key
     */
    private String convertS3ProtocolUrlToObjectUrl(String s3ProtocolUrl) {
        // s3:// 제거
        String withoutProtocol = s3ProtocolUrl.substring(5);
        
        // 버킷 이름과 키 분리
        int firstSlashIndex = withoutProtocol.indexOf('/');
        String bucketName = withoutProtocol.substring(0, firstSlashIndex);
        String key = withoutProtocol.substring(firstSlashIndex + 1);
        
        // 객체 URL 생성 (리전은 ap-northeast-2로 가정)
        return String.format("https://%s.s3.ap-northeast-2.amazonaws.com/%s", bucketName, key);
    }
} 