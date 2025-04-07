package com.min.i.memory_BE.domain.album.controller;

import com.min.i.memory_BE.domain.album.entity.Story;
import com.min.i.memory_BE.domain.album.service.StoryService;
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

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/stories")
@Tag(name = "스토리 API", description = "스토리 생성 및 조회 API")
@RequiredArgsConstructor
@Slf4j
public class StoryController {

    private final StoryService storyService;
    
    @PostMapping("/generate")
    @Operation(summary = "스토리 생성", description = "미디어에 연결된 질문과 답변을 기반으로 스토리를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스토리 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "미디어를 찾을 수 없음")
    })
    public ResponseEntity<?> generateStory(
            @Parameter(description = "미디어 ID", required = true)
            @RequestParam Long mediaId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("스토리 생성 요청: mediaId={}", mediaId);
        
        try {
            Story story = storyService.generateStory(mediaId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "스토리가 성공적으로 생성되었습니다.");
            response.put("data", Map.of(
                "storyId", story.getId(),
                "mediaId", mediaId,
                "content", story.getContent(),
                "createdAt", story.getCreatedAt()
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("스토리 생성 중 오류 발생", e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
} 