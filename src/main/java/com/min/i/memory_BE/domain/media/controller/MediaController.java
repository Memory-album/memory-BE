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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Tag(name = "Media API", description = "미디어 관리 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MediaController {
    private final MediaService mediaService;
    private static final Logger log = LoggerFactory.getLogger(MediaController.class);

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
     * 2. 그룹 내 앨범의 모든 미디어 조회 (페이징)
     */
    @Operation(
            summary = "앨범 미디어 목록 조회",
            description = "그룹 내 특정 앨범의 모든 미디어를 페이징하여 조회합니다."
    )
    @GetMapping("/groups/{groupId}/albums/{albumId}/media")
    public ResponseEntity<?> getAlbumMedia(
            @Parameter(description = "그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "앨범 ID") @PathVariable Long albumId,
            @Parameter(description = "페이징 정보") @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            // 서비스 호출 - 인증 및 권한 검증은 서비스에서 처리
            // 이미 DTO로 변환된 결과를 반환받으므로 별도의 변환 과정 불필요
            Page<MediaResponseDto> mediaPage = mediaService.getAllAlbumMediaWithAuth(
                groupId, albumId, pageable, userDetails.getUser()
            );
            
            // 이미 DTO로 변환된 상태이므로 PageResponseDto 생성 방식 변경
            PageResponseDto<MediaResponseDto> responseDto = PageResponseDto.<MediaResponseDto>builder()
                    .content(mediaPage.getContent())
                    .pageNumber(mediaPage.getNumber())
                    .pageSize(mediaPage.getSize())
                    .totalElements(mediaPage.getTotalElements())
                    .totalPages(mediaPage.getTotalPages())
                    .first(mediaPage.isFirst())
                    .last(mediaPage.isLast())
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(responseDto));
            
        } catch (EntityNotFoundException e) {
            // 엔티티를 찾을 수 없는 경우 상세 메시지 포함
            return ResponseEntity.status(404).body(ApiResponse.error(ErrorCode.ENTITY_NOT_FOUND, e.getMessage()));
        } catch (Exception e) {
            // 상세한 오류 정보 로깅
            log.error("미디어 조회 중 서버 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "미디어 조회 중 서버 오류: " + e.getMessage()));
        }
    }
    
    /**
     * 3. 미디어 삭제
     */
    @Operation(
            summary = "미디어 삭제",
            description = "특정 그룹과 앨범의 미디어를 삭제합니다. S3에서도 파일이 삭제됩니다."
    )
    @DeleteMapping("/groups/{groupId}/albums/{albumId}/media/{mediaId}")
    public ResponseEntity<ApiResponse<?>> deleteMedia(
            @Parameter(description = "그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "앨범 ID") @PathVariable Long albumId,
            @Parameter(description = "미디어 ID") @PathVariable Long mediaId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            // 서비스 호출 - 인증 및 권한 검증은 서비스에서 처리
            mediaService.deleteMedia(groupId, albumId, mediaId, userDetails.getUser());
            
            return ResponseEntity.ok(ApiResponse.success("미디어가 성공적으로 삭제되었습니다."));
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(ErrorCode.ENTITY_NOT_FOUND));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }
}