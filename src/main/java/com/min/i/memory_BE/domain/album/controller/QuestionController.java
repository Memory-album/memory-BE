package com.min.i.memory_BE.domain.album.controller;

import com.min.i.memory_BE.domain.album.dto.QuestionDto;
import com.min.i.memory_BE.domain.album.service.QuestionService;
import com.min.i.memory_BE.domain.user.security.CustomUserDetails;
import com.min.i.memory_BE.global.error.exception.EntityNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import com.min.i.memory_BE.domain.media.service.MediaService;
import com.min.i.memory_BE.domain.album.repository.AlbumRepository;
import com.min.i.memory_BE.domain.media.entity.Media;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
@Tag(name = "Question API", description = "질문 관리 API")
public class QuestionController {

    private final QuestionService questionService;
    private final MediaService mediaService;
    private final AlbumRepository albumRepository;
    
    @GetMapping("/{questionId}")
    @Operation(summary = "질문 상세 조회", description = "질문 ID에 해당하는 질문 상세 정보를 반환합니다.")
    public ResponseEntity<?> getQuestionDetail(@PathVariable Long questionId) {
        try {
            log.info("질문 ID {} 상세 조회 요청", questionId);
            
            // 서비스 계층 사용
            QuestionDto.Response question = questionService.getQuestionById(questionId);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "질문 상세 조회 성공",
                "data", question
            ));
            
        } catch (EntityNotFoundException e) {
            log.error("질문을 찾을 수 없음: {}", e.getMessage());
            return ResponseEntity.status(404).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("질문 상세 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "질문 상세 조회 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{questionId}")
    @Operation(summary = "질문 수정", description = "질문 ID에 해당하는 질문을 수정합니다.")
    public ResponseEntity<?> updateQuestion(
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionDto.Update requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        try {
            log.info("질문 수정 요청: questionId={}, content={}", 
                questionId, requestDto.getContent());
            
            // 질문 수정
            QuestionDto.Response response = questionService.updateQuestion(questionId, requestDto);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "질문이 성공적으로 수정되었습니다",
                "data", response
            ));
            
        } catch (EntityNotFoundException e) {
            log.error("질문 수정 중 엔티티를 찾을 수 없음: {}", e.getMessage());
            return ResponseEntity.status(404).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("질문 수정 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "질문 수정 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/media/{mediaId}")
    @Operation(summary = "미디어별 질문 조회", description = "미디어 ID에 해당하는 질문 목록을 반환합니다.")
    public ResponseEntity<?> getQuestionsByMediaId(
            @PathVariable Long mediaId,
            @RequestParam(required = false, defaultValue = "all") String filter) {
        try {
            log.info("미디어 ID {} 관련 질문 조회 요청, 필터: {}", mediaId, filter);
            
            List<QuestionDto.Response> questions;
            String message;
            
            // 필터에 따라 다른 질문 목록 조회
            switch (filter) {
                case "unanswered":
                    // 미답변 질문만 조회
                    questions = questionService.getUnansweredQuestionsByMediaId(mediaId);
                    message = questions.isEmpty() ? "해당 미디어에 대한 미답변 질문이 없습니다" : "미답변 질문 조회 성공";
                    break;
                case "answered":
                    // 답변된 질문만 조회
                    questions = questionService.getAnsweredQuestionsByMediaId(mediaId);
                    message = questions.isEmpty() ? "해당 미디어에 대한 답변된 질문이 없습니다" : "답변된 질문 조회 성공";
                    break;
                default:
                    // 모든 질문 조회
                    questions = questionService.getQuestionsByMediaId(mediaId);
                    message = questions.isEmpty() ? "해당 미디어에 대한 질문이 없습니다" : "질문 조회 성공";
                    break;
            }
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", message,
                "data", Map.of("questions", questions)
            ));
            
        } catch (EntityNotFoundException e) {
            log.error("미디어를 찾을 수 없음: {}", e.getMessage());
            return ResponseEntity.status(404).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("질문 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "질문 조회 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    @PostMapping(value = "/with-media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "미디어 업로드와 질문 생성", description = "미디어 파일을 업로드하고 해당 미디어에 대한 질문을 함께 생성합니다.")
    public ResponseEntity<?> createQuestionWithMedia(
            @RequestPart(value = "file", required = true) MultipartFile file,
            @RequestParam(value = "content", required = true) String content,
            @RequestParam(value = "groupId", required = true) Long groupId,
            @RequestParam(value = "albumId", required = true) Long albumId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        try {
            log.info("미디어 업로드 및 질문 생성 요청: groupId={}, albumId={}, contentLength={}", 
                    groupId, albumId, content.length());
            
            // 1. 미디어 업로드
            Media uploadedMedia = mediaService.uploadMedia(file, groupId, albumId, userDetails.getUser());
            
            // 2. 질문 생성 DTO 생성
            QuestionDto.Create questionDto = new QuestionDto.Create(uploadedMedia.getId(), content);
            
            // 3. 질문 생성
            QuestionDto.Response response = questionService.createUserQuestion(questionDto);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "미디어 업로드 및 질문 생성이 성공적으로 완료되었습니다",
                "data", Map.of(
                    "mediaId", uploadedMedia.getId(),
                    "mediaUrl", uploadedMedia.getFileUrl(),
                    "question", response
                )
            ));
            
        } catch (EntityNotFoundException e) {
            log.error("미디어 업로드 및 질문 생성 중 엔티티를 찾을 수 없음: {}", e.getMessage());
            return ResponseEntity.status(404).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("미디어 업로드 및 질문 생성 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "미디어 업로드 및 질문 생성 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
} 