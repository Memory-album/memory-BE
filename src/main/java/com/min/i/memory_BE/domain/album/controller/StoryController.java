package com.min.i.memory_BE.domain.album.controller;

import com.min.i.memory_BE.domain.album.dto.StoryDto;
import com.min.i.memory_BE.domain.album.entity.Story;
import com.min.i.memory_BE.domain.album.service.StoryService;
import com.min.i.memory_BE.domain.user.security.CustomUserDetails;
import com.min.i.memory_BE.global.error.exception.DuplicateResourceException;
import com.min.i.memory_BE.global.error.exception.EntityNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/stories")
@Tag(name = "Story API", description = "스토리 관리 API")
@RequiredArgsConstructor
@Slf4j
public class StoryController {

    private final StoryService storyService;
    
    @GetMapping("/{mediaId}")
    @Operation(summary = "스토리 조회", description = "미디어 ID로 스토리를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스토리 조회 성공"),
            @ApiResponse(responseCode = "404", description = "스토리를 찾을 수 없음")
    })
    public ResponseEntity<?> getStory(
            @Parameter(description = "미디어 ID", required = true)
            @PathVariable Long mediaId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("스토리 조회 요청: mediaId={}", mediaId);
        
        try {
            Story story = storyService.getStoryByMediaId(mediaId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "스토리를 성공적으로 조회했습니다.");
            response.put("data", Map.of(
                "storyId", story.getId(),
                "mediaId", mediaId,
                "content", story.getContent(),
                "createdAt", story.getCreatedAt()
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("스토리 조회 중 오류 발생", e);
            return ResponseEntity.status(404).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    @PutMapping("/{storyId}")
    @Operation(summary = "스토리 수정", description = "스토리 ID에 해당하는 스토리를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스토리 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "스토리를 찾을 수 없음")
    })
    public ResponseEntity<?> updateStory(
            @Parameter(description = "스토리 ID", required = true)
            @PathVariable Long storyId,
            @Valid @RequestBody StoryDto.Update requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        try {
            log.info("스토리 수정 요청: storyId={}, content={}", 
                storyId, requestDto.getContent());
            
            // 스토리 수정
            StoryDto.Response response = storyService.updateStory(storyId, requestDto);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "스토리가 성공적으로 수정되었습니다",
                "data", response
            ));
            
        } catch (EntityNotFoundException e) {
            log.error("스토리 수정 중 엔티티를 찾을 수 없음: {}", e.getMessage());
            return ResponseEntity.status(404).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("스토리 수정 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "스토리 수정 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping("/generate")
    @Operation(summary = "스토리 생성", description = "미디어에 연결된 질문과 답변을 기반으로 스토리를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스토리 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "409", description = "이미 스토리가 존재함"),
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
        } catch (DuplicateResourceException e) {
            log.warn("이미 스토리가 존재합니다: {}", e.getMessage());
            return ResponseEntity.status(409).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("스토리 생성 중 오류 발생", e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
} 