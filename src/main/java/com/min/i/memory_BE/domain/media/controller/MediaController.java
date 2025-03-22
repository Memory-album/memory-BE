package com.min.i.memory_BE.domain.media.controller;

import com.min.i.memory_BE.domain.media.dto.response.MediaResponseDto;
import com.min.i.memory_BE.domain.media.entity.Media;
import com.min.i.memory_BE.domain.media.service.MediaService;
import com.min.i.memory_BE.domain.user.security.CustomUserDetails;
import com.min.i.memory_BE.global.dto.PageResponseDto;
import com.min.i.memory_BE.global.response.ApiResponse;
import com.min.i.memory_BE.global.error.exception.EntityNotFoundException;
import com.min.i.memory_BE.global.error.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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

        // 트랜잭션 내에서 DTO로 변환된 결과를 직접 받아옴
        List<MediaResponseDto> responseDtoList = mediaService.getRecentMediaDtoByAlbumWithAuth(albumId, limit, userDetails.getUser());
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
    public ResponseEntity<?> getAlbumMedia(
            @Parameter(description = "그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "앨범 ID") @PathVariable Long albumId,
            @Parameter(description = "페이징 정보") @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            // 요청 정보 로깅
            System.out.println("Requesting album media - groupId: " + groupId + ", albumId: " + albumId + 
                ", page: " + pageable.getPageNumber() + ", size: " + pageable.getPageSize() + 
                ", sort: " + (pageable.getSort() != null ? pageable.getSort().toString() : "null"));
            
            // 사용자 정보 로깅
            if (userDetails == null || userDetails.getUser() == null) {
                return ResponseEntity.status(401).body(ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE));
            }

            // 1. 기본 페이지와 사이즈를 사용한 간단한 Pageable 객체 생성
            Pageable simplePage = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

            // 사용자 인증 및 그룹/앨범 접근 권한 검증은 서비스에서 처리됨
            Page<Media> mediaPage;
            try {
                mediaPage = mediaService.getAllAlbumMediaWithAuth(groupId, albumId, simplePage, userDetails.getUser());
            } catch (Exception e) {
                System.err.println("Service error: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(500).body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
            }
            
            if (mediaPage == null) {
                System.out.println("Media page is null");
                // 빈 페이지 생성
                Page<Media> emptyPage = new PageImpl<>(Collections.emptyList(), simplePage, 0);
                return ResponseEntity.ok(ApiResponse.success(PageResponseDto.of(emptyPage, MediaResponseDto::from)));
            }
            
            if (mediaPage.isEmpty()) {
                System.out.println("Media page is empty");
                // 빈 페이지 생성
                Page<Media> emptyPage = new PageImpl<>(Collections.emptyList(), simplePage, 0);
                return ResponseEntity.ok(ApiResponse.success(PageResponseDto.of(emptyPage, MediaResponseDto::from)));
            }
            
            System.out.println("Media page has " + mediaPage.getTotalElements() + " elements");
            
            PageResponseDto<MediaResponseDto> responseDto;
            try {
                responseDto = PageResponseDto.of(mediaPage, MediaResponseDto::from);
            } catch (Exception e) {
                System.err.println("Error converting to DTO: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(500).body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
            }
            
            return ResponseEntity.ok(ApiResponse.success(responseDto));
        } catch (EntityNotFoundException e) {
            System.err.println("Entity not found: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(404).body(ApiResponse.error(ErrorCode.ENTITY_NOT_FOUND));
        } catch (Exception e) {
            System.err.println("Error in getAlbumMedia: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }
}