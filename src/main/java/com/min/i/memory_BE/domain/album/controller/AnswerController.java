package com.min.i.memory_BE.domain.album.controller;

import com.min.i.memory_BE.domain.album.entity.Answer;
import com.min.i.memory_BE.domain.album.entity.Question;
import com.min.i.memory_BE.domain.album.service.AnswerService;
import com.min.i.memory_BE.domain.user.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
@Tag(name = "Answer API", description = " 답변 관리 API")
public class AnswerController {

    private final AnswerService answerService;
    
    @PostMapping
    @Operation(summary = "답변 생성", description = "미디어에 대한 텍스트 또는 음성 답변을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "답변 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "미디어를 찾을 수 없음")
    })
    public ResponseEntity<?> createAnswer(
            @Parameter(description = "미디어 ID", required = true)
            @RequestParam("mediaId") Long mediaId,
            
            @Parameter(description = "질문 ID", required = true)
            @RequestParam("questionId") Long questionId,
            
            @Parameter(description = "텍스트 답변 내용")
            @RequestParam(value = "textContent", required = false) String textContent,
            
            @Parameter(description = "음성 답변 파일")
            @RequestParam(value = "audioFile", required = false) MultipartFile audioFile,
            
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        try {
            log.info("답변 생성 요청: 미디어 ID={}, 질문 ID={}, 텍스트 있음={}, 오디오 있음={}", 
                mediaId,
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
            
            // 미디어에 연결된 질문 조회
            List<Question> questions = answerService.getQuestionsByMediaId(mediaId);
            
            if (questions.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "미디어에 연결된 질문이 없습니다."
                ));
            }
            
            // questionId를 전달하여 답변 저장
            Answer savedAnswer = answerService.saveAnswer(
                    mediaId,
                    questionId,
                    userDetails.getUser(), 
                    textContent, 
                    audioFile);
            
            // 질문 목록을 간단한 형태로 변환
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
            responseData.put("answerId", savedAnswer.getId());
            responseData.put("content", savedAnswer.getContent());
            responseData.put("mediaId", mediaId);
            responseData.put("questionId", questionId);
            responseData.put("questions", questionsList);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "답변이 성공적으로 저장되었습니다.",
                "data", responseData
            ));
            
        } catch (Exception e) {
            log.error("답변 생성 중 오류 발생", e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/media/{mediaId}")
    @Operation(summary = "미디어별 답변 조회", description = "미디어 ID에 해당하는 답변 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "답변 조회 성공"),
            @ApiResponse(responseCode = "404", description = "미디어를 찾을 수 없음")
    })
    public ResponseEntity<?> getAnswersByMediaId(
            @Parameter(description = "미디어 ID", required = true)
            @PathVariable Long mediaId,
            
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        try {
            List<Answer> answers = answerService.getAnswersByMediaIdAndUserId(mediaId, userDetails.getUser().getId());
            
            List<Map<String, Object>> answersList = answers.stream()
                .map(a -> {
                    Map<String, Object> answerMap = new HashMap<>();
                    answerMap.put("id", a.getId());
                    answerMap.put("content", a.getContent());
                    answerMap.put("createdAt", a.getCreatedAt());
                    answerMap.put("updatedAt", a.getUpdatedAt());
                    return answerMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "답변 조회 성공",
                "data", Map.of(
                    "mediaId", mediaId,
                    "answers", answersList
                )
            ));
            
        } catch (Exception e) {
            log.error("답변 조회 중 오류 발생", e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 음성 파일을 텍스트로 변환하는 테스트용 엔드포인트
     */
    @PostMapping("/speech-to-text")
    @Operation(summary = "음성 파일을 텍스트로 변환", description = "음성 파일을 텍스트로 변환합니다 (테스트용).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "변환 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<?> testSpeechToText(HttpServletRequest request) {
        try {
            log.info("요청 Content-Type: {}", request.getContentType());
            
            if (!request.getContentType().startsWith("multipart/form-data")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "요청이 multipart/form-data 형식이 아닙니다."
                ));
            }
            
            // 수동으로 멀티파트 요청 처리
            if (request instanceof MultipartHttpServletRequest) {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
                MultipartFile audioFile = multipartRequest.getFile("audioFile");
                
                if (audioFile == null || audioFile.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "음성 파일이 필요합니다."
                    ));
                }
                
                String convertedText = answerService.getSpeechToTextService().convertSpeechToText(audioFile);
                
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "음성이 성공적으로 텍스트로 변환되었습니다.",
                    "text", convertedText
                ));
            } else {
                log.error("요청이 MultipartHttpServletRequest로 캐스팅되지 않습니다.");
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "멀티파트 요청 처리에 실패했습니다."
                ));
            }
        } catch (Exception e) {
            log.error("음성-텍스트 변환 중 오류 발생", e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
}