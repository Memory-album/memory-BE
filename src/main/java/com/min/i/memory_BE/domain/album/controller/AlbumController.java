package com.min.i.memory_BE.domain.album.controller;

import com.min.i.memory_BE.domain.album.dto.request.AlbumRequestDto;
import com.min.i.memory_BE.domain.album.dto.response.AlbumResponseDto;
import com.min.i.memory_BE.domain.album.dto.response.GroupAlbumListResponseDto;
import com.min.i.memory_BE.domain.album.entity.Album;
import com.min.i.memory_BE.domain.album.service.AlbumService;
import com.min.i.memory_BE.domain.user.security.CustomUserDetails;
import com.min.i.memory_BE.global.error.exception.EntityNotFoundException;
import com.min.i.memory_BE.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/albums")
@Tag(name = "Album API", description = "앨범 관리 API")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @Operation(summary = "앨범 생성", description = "새 앨범을 생성합니다. 로그인한 사용자 정보가 자동으로 적용됩니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AlbumResponseDto>> createAlbum(
            @ModelAttribute AlbumRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        // 로그인한 사용자 ID 설정
        if (userDetails != null) {
            request.setUserId(userDetails.getUser().getId());
        }
        
        // 그룹 ID 검증
        if (request.getGroupId() == null) {
            throw new EntityNotFoundException("Group ID is required");
        }
        
        Album album = albumService.createAlbum(request);
        return ResponseEntity.ok(ApiResponse.success(AlbumResponseDto.fromEntity(album)));
    }
    
    @Operation(
        summary = "그룹의 앨범 목록 조회", 
        description = "그룹 ID로 해당 그룹에 속한 앨범 목록을 조회합니다. 각 앨범의 제목, ID와 함께 최근 미디어 썸네일도 함께 제공합니다."
    )
    @GetMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse<List<GroupAlbumListResponseDto>>> getAlbumsByGroup(
            @Parameter(description = "그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "썸네일로 표시할 최근 미디어 개수") @RequestParam(defaultValue = "5") int thumbnailCount,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        List<GroupAlbumListResponseDto> albums = albumService.getAlbumsByGroupId(
            groupId, 
            thumbnailCount, 
            userDetails.getUser()
        );
        
        return ResponseEntity.ok(ApiResponse.success(albums));
    }

    @Operation(summary = "앨범 수정", description = "앨범 정보를 수정합니다. 제목, 설명, 테마, 썸네일 등을 변경할 수 있습니다.")
    @PutMapping(value = "/{albumId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AlbumResponseDto>> updateAlbum(
            @Parameter(description = "앨범 ID") @PathVariable Long albumId,
            @ModelAttribute AlbumRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        // 로그인한 사용자 ID 설정
        if (userDetails != null) {
            request.setUserId(userDetails.getUser().getId());
        }
        
        Album updatedAlbum = albumService.updateAlbum(albumId, request);
        return ResponseEntity.ok(ApiResponse.success(AlbumResponseDto.fromEntity(updatedAlbum)));
    }
    
    @Operation(summary = "앨범 삭제", description = "앨범을 삭제합니다. 앨범과 연결된 모든 미디어는 유지됩니다.")
    @DeleteMapping("/{albumId}")
    public ResponseEntity<ApiResponse<Void>> deleteAlbum(
            @Parameter(description = "앨범 ID") @PathVariable Long albumId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        albumService.deleteAlbum(albumId, userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
