package com.min.i.memory_BE.domain.media.controller;

import com.min.i.memory_BE.domain.media.service.MediaAnalysisService;
import com.min.i.memory_BE.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "미디어 분석 API", description = "미디어 AI 분석 결과 관련 API")
@RestController
@RequestMapping("/api/v1/media-analysis")
@RequiredArgsConstructor
public class MediaAnalysisController {

    private final MediaAnalysisService mediaAnalysisService;

    @Operation(summary = "AI 분석 결과 처리", description = "미디어의 AI 분석 결과를 처리하고 저장합니다.")
    @PostMapping("/{mediaId}")
    public ResponseEntity<ApiResponse<Void>> processAnalysisResult(
            @PathVariable Long mediaId,
            @RequestBody String analysisResult) {
        
        mediaAnalysisService.processAnalysisResult(mediaId, analysisResult);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
} 