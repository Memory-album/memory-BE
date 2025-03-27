package com.min.i.memory_BE.domain.album.controller;

import com.min.i.memory_BE.domain.album.entity.Question;
import com.min.i.memory_BE.domain.album.repository.QuestionRepository;
import com.min.i.memory_BE.domain.media.entity.Media;
import com.min.i.memory_BE.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionController {

    private final QuestionRepository questionRepository;

    @GetMapping("/{questionId}")
    @Operation(summary = "질문 상세 조회", description = "질문 ID에 해당하는 질문 상세 정보를 반환합니다.")
    public ResponseEntity<?> getQuestionDetail(@PathVariable Long questionId) {
        try {
            log.info("질문 ID {} 상세 조회 요청", questionId);
            
            Question question = questionRepository.findByIdWithMediaAndUploader(questionId)
                .orElseThrow(() -> new RuntimeException("질문을 찾을 수 없습니다: " + questionId));
            
            // 미디어 및 업로더 정보 추출
            Media media = question.getMedia();
            User uploader = media.getUploadedBy();
            
            // 응답 데이터 구성 (간소화)
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("userName", uploader != null ? uploader.getName() : "알 수 없음");
            responseData.put("content", question.getContent());
            responseData.put("imageUrl", media.getImageUrl());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "질문 상세 조회 성공");
            response.put("data", responseData);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("질문 상세 조회 중 오류 발생: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "질문 상세 조회 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/media/{mediaId}")
    @Operation(summary = "미디어별 질문 조회", description = "미디어 ID에 해당하는 질문 목록을 반환합니다.")
    public ResponseEntity<?> getQuestionsByMediaId(@PathVariable Long mediaId) {
        try {
            log.info("미디어 ID {} 관련 질문 조회 요청", mediaId);
            
            List<Question> questions = questionRepository.findByMediaIdWithMediaAndUploader(mediaId);
            
            if (questions.isEmpty()) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("questions", new ArrayList<>());
                
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "해당 미디어에 대한 질문이 없습니다.");
                response.put("data", responseData);
                
                return ResponseEntity.ok(response);
            }
            
            // 미디어 및 업로더 정보 추출
            Media media = questions.get(0).getMedia();
            User uploader = media.getUploadedBy();
            
            // 사용자 정보 맵 생성
            Map<String, Object> uploaderInfo = new HashMap<>();
            if (uploader != null) {
                uploaderInfo.put("id", uploader.getId());
                uploaderInfo.put("name", uploader.getName());
                uploaderInfo.put("profileImgUrl", uploader.getProfileImgUrl());
            }
            
            // 미디어 정보 맵 생성
            Map<String, Object> mediaInfo = new HashMap<>();
            mediaInfo.put("id", media.getId());
            mediaInfo.put("imageUrl", media.getImageUrl());
            mediaInfo.put("fileUrl", media.getFileUrl());
            
            // 질문 목록을 변환하여 반환
            List<Map<String, Object>> questionsList = questions.stream()
                .map(q -> {
                    Map<String, Object> questionMap = new HashMap<>();
                    questionMap.put("id", q.getId());
                    questionMap.put("content", q.getContent());
                    questionMap.put("category", q.getCategory());
                    questionMap.put("level", q.getLevel());
                    return questionMap;
                })
                .collect(Collectors.toList());
            
            // 응답 데이터 구성
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("mediaId", mediaId);
            responseData.put("media", mediaInfo);
            responseData.put("uploader", uploaderInfo);
            responseData.put("questions", questionsList);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "질문 조회 성공");
            response.put("data", responseData);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("질문 조회 중 오류 발생: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "질문 조회 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
} 