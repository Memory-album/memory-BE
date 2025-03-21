package com.min.i.memory_BE.domain.media.controller;

import com.min.i.memory_BE.domain.media.dto.response.MediaResponseDto;
import com.min.i.memory_BE.domain.media.entity.Media;
import com.min.i.memory_BE.domain.media.service.MediaService;
import com.min.i.memory_BE.domain.user.security.CustomUserDetails;
import com.min.i.memory_BE.global.dto.PageResponseDto;
import com.min.i.memory_BE.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Media API", description = "미디어 관리 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MediaController {
    private final MediaService mediaService;

    /**
     * 1. 메인페이지용 - 앨범의 최근 미디어 조회 (story 필드는 우선 null)
     */
    @Operation(
            summary = "앨범 최근 미디어 조회",
            description = "특정 앨범의 최근 업로드된 미디어를 조회합니다. story 필드는 우선 null 값으로 반환됩니다."
    )
    @GetMapping("/albums/{albumId}/recent-media")
    public ResponseEntity<ApiResponse<List<MediaResponseDto>>> getRecentMediaByAlbum(
            @Parameter(description = "앨범 ID") @PathVariable Long albumId,
            @Parameter(description = "조회할 미디어 개수") @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 사용자 인증 및 앨범 접근 권한 검증은 서비스에서 처리됨
        List<Media> recentMedia = mediaService.getRecentMediaByAlbumWithAuth(albumId, limit, userDetails.getUser());
        List<MediaResponseDto> responseDtoList = MediaResponseDto.fromList(recentMedia);
        return ResponseEntity.ok(ApiResponse.success(responseDtoList));
    }

    /**
     * 2. 그룹 내 앨범의 모든 미디어 조회 (story 필드는 우선 null)
     */
    @Operation(
            summary = "앨범 미디어 목록 조회",
            description = "그룹 내 특정 앨범의 모든 미디어를 조회합니다. story 필드는 우선 null 값으로 반환됩니다."
    )
    @GetMapping("/groups/{groupId}/albums/{albumId}/media")
    public ResponseEntity<ApiResponse<PageResponseDto<MediaResponseDto>>> getAlbumMedia(
            @Parameter(description = "그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "앨범 ID") @PathVariable Long albumId,
            @Parameter(description = "페이징 정보") @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 사용자 인증 및 그룹/앨범 접근 권한 검증은 서비스에서 처리됨
        Page<Media> mediaPage = mediaService.getAllAlbumMediaWithAuth(groupId, albumId, pageable, userDetails.getUser());
        PageResponseDto<MediaResponseDto> responseDto = PageResponseDto.of(mediaPage, MediaResponseDto::from);
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }
}