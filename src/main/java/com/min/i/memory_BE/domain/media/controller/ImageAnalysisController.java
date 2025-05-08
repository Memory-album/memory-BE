package com.min.i.memory_BE.domain.media.controller;

import com.min.i.memory_BE.domain.album.entity.Album;
import com.min.i.memory_BE.domain.album.repository.AlbumRepository;
import com.min.i.memory_BE.domain.media.client.FastApiClient;
import com.min.i.memory_BE.domain.media.entity.Media;
import com.min.i.memory_BE.domain.media.service.MediaAnalysisService;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.repository.UserRepository;
import com.min.i.memory_BE.global.error.exception.EntityNotFoundException;
import com.min.i.memory_BE.global.service.S3Service;
import com.min.i.memory_BE.domain.media.enums.MediaType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Tag(name = "ImageAnalysis API", description = "이미지 업로드 및 분석 관련 API")
public class ImageAnalysisController {

    private final S3Service s3Service;
    private final FastApiClient fastApiClient;
    private final MediaAnalysisService mediaAnalysisService;
    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;

    @PostMapping(
        value = "/analyze", 
        consumes = {"multipart/form-data"}
    )
    @Operation(
        summary = "이미지 분석 요청", 
        description = "이미지를 업로드하여 분석합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "이미지 분석 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "사용자 또는 앨범을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> analyzeImage(
            @Parameter(description = "이미지 파일", required = true)
            @RequestPart("image") MultipartFile image,
            
            @Parameter(description = "앨범 ID", required = true)
            @RequestParam("albumId") Long albumId,
            
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam("userId") Long userId,
            
            @Parameter(description = "인증 토큰 (Bearer 인증)", required = false)
            @RequestHeader(value = "Authorization", required = false) String authHeader) 
    {
        try {
            // 인증 토큰 추출 (Bearer 접두사 제거)
            String authToken = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authToken = authHeader.substring(7);
            }
            
            // 1. 이미지 검증
            if (image.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "이미지 파일이 비어있습니다."
                ));
            }

            // 2. 사용자와 앨범 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));

            Album album = albumRepository.findById(albumId)
                    .orElseThrow(() -> new EntityNotFoundException("앨범을 찾을 수 없습니다: " + albumId));

            // 3. 미디어 엔티티 생성 (분석 결과 저장용)
            Media media = mediaAnalysisService.createEmptyMedia(user, album);

            // 4. S3에 이미지 업로드
            String imageUrl = s3Service.uploadAlbumImage(image, album.getId());
            log.info("이미지 S3 업로드 완료: {}", imageUrl);

            // 5. 이미지 URL 및 파일 정보 업데이트
            media.setImageUrl(imageUrl);
            media.setFileUrl(imageUrl);
            media.setFileType(determineMediaType(image.getContentType()));
            media.setOriginalFilename(image.getOriginalFilename());
            
            // file_size가 NULL이 되지 않도록 명시적으로 설정
            if (image.getSize() <= 0) {
                media.setFileSize(0L); // 기본값 설정
            } else {
                media.setFileSize(image.getSize());
            }
            
            mediaAnalysisService.updateMedia(media);

            // 6. FastAPI로 이미지 URL 전송하여 분석 요청
            Map<String, Object> analysisResult = fastApiClient.analyzeImageByUrl(imageUrl, authToken);
            log.info("FastAPI 서버로부터 분석 결과 수신 완료");

            // 7. 분석 결과 처리
            Map<String, Object> processedResult = mediaAnalysisService.processAnalysisResult(media.getId(), analysisResult);

            // 8. 응답 구성
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "이미지 분석이 완료되었습니다",
                    "data", Map.of(
                            "mediaId", media.getId(),
                            "imageUrl", imageUrl,
                            "albumId", album.getId(),
                            "userId", user.getId(),
                            "questions", processedResult.getOrDefault("questions", "질문이 생성되지 않았습니다")
                    )
            ));

        } catch (EntityNotFoundException e) {
            log.error("엔티티를 찾을 수 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("이미지 분석 요청 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "이미지 분석 요청 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/analysis/{mediaId}")
    @Operation(summary = "분석 결과 처리", description = "FastAPI로부터 받은 분석 결과를 처리합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "분석 결과 처리 성공"),
        @ApiResponse(responseCode = "404", description = "미디어를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> processAnalysisResult(
            @Parameter(description = "미디어 ID", required = true)
            @PathVariable Long mediaId,
            
            @Parameter(description = "분석 결과 데이터", required = true)
            @RequestBody Map<String, Object> analysisData
    ) {
        try {
            log.info("분석 결과 수신: mediaId={}", mediaId);

            // 분석 결과 및 질문 저장
            Map<String, Object> processedResult = mediaAnalysisService.processAnalysisResult(mediaId, analysisData);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "분석 결과가 성공적으로 처리되었습니다",
                    "data", Map.of(
                            "mediaId", mediaId,
                            "questions", processedResult.getOrDefault("questions", "질문이 생성되지 않았습니다")
                    )
            ));

        } catch (EntityNotFoundException e) {
            log.error("미디어를 찾을 수 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("분석 결과 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "분석 결과 처리 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/questions/create")
    @Operation(summary = "질문 생성 처리", description = "FastAPI로부터 받은 질문 생성 결과를 처리합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "질문 생성 결과 처리 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "미디어를 찾을 수 없음"), 
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> processQuestions(
            @Parameter(description = "질문 생성 결과 데이터", required = true)
            @RequestBody Map<String, Object> questionsData
    ) {
        try {
            log.info("질문 생성 결과 수신");

            // mediaId 파라미터 확인
            if (!questionsData.containsKey("mediaId")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "mediaId가 필요합니다"
                ));
            }

            Long mediaId = Long.valueOf(questionsData.get("mediaId").toString());

            // 분석 결과 및 질문 저장
            Map<String, Object> processedResult = mediaAnalysisService.processAnalysisResult(mediaId, questionsData);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "질문 생성 결과가 성공적으로 처리되었습니다",
                    "data", Map.of(
                            "mediaId", mediaId,
                            "questions", processedResult.getOrDefault("questions", "질문이 생성되지 않았습니다")
                    )
            ));

        } catch (EntityNotFoundException e) {
            log.error("미디어를 찾을 수 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("질문 생성 결과 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "질문 생성 결과 처리 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    // 미디어 타입 결정 헬퍼 메서드
    private MediaType determineMediaType(String contentType) {
        if (contentType != null && contentType.startsWith("image/")) {
            return MediaType.IMAGE;
        } else if (contentType != null && contentType.startsWith("video/")) {
            return MediaType.VIDEO;
        }
        return MediaType.IMAGE; // 기본값
    }
}
