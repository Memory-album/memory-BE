package com.min.i.memory_BE.domain.album.controller;

import com.min.i.memory_BE.domain.album.entity.Question;
import com.min.i.memory_BE.domain.album.repository.QuestionRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
public class QuestionController {

    private final QuestionRepository questionRepository;

    @GetMapping("/media/{mediaId}")
    @Operation(summary = "미디어별 질문 조회", description = "미디어 ID에 해당하는 질문 목록을 반환합니다.")
    public ResponseEntity<?> getQuestionsByMediaId(@PathVariable Long mediaId) {
        try {
            log.info("미디어 ID {} 관련 질문 조회 요청", mediaId);
            
            List<Question> questions = questionRepository.findByMediaId(mediaId);
            
            if (questions.isEmpty()) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("questions", new ArrayList<>());
                
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "해당 미디어에 대한 질문이 없습니다.");
                response.put("data", responseData);
                
                return ResponseEntity.ok(response);
            }
            
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
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("mediaId", mediaId);
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