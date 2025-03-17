package com.min.i.memory_BE.domain.album.controller;

import com.min.i.memory_BE.domain.album.entity.Answer;
import com.min.i.memory_BE.domain.album.service.AnswerService;
import com.min.i.memory_BE.domain.user.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;
    
    @PostMapping
    public ResponseEntity<?> createAnswer(
            @RequestParam("questionId") Long questionId,
            @RequestParam(value = "textContent", required = false) String textContent,
            @RequestParam(value = "audioFile", required = false) MultipartFile audioFile,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        try {
            log.info("답변 생성 요청: 질문 ID={}, 텍스트 있음={}, 오디오 있음={}", 
                questionId, 
                (textContent != null && !textContent.isEmpty()), 
                (audioFile != null && !audioFile.isEmpty()));
            
            // 텍스트나 오디오 중 하나는 필수
            if ((textContent == null || textContent.trim().isEmpty()) && 
                (audioFile == null || audioFile.isEmpty())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "텍스트 또는 음성 답변이 필요합니다."
                ));
            }
            
            Answer savedAnswer = answerService.saveAnswer(
                    questionId, 
                    userDetails.getUser(), 
                    textContent, 
                    audioFile);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "답변이 성공적으로 저장되었습니다.",
                "answerId", savedAnswer.getId(),
                "content", savedAnswer.getContent()
            ));
            
        } catch (Exception e) {
            log.error("답변 생성 중 오류 발생", e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    // 기타 필요한 엔드포인트들...
}