package com.min.i.memory_BE.domain.album.controller;

import com.min.i.memory_BE.domain.album.dto.request.AlbumRequestDto;
import com.min.i.memory_BE.domain.album.dto.response.AlbumResponseDto;
import com.min.i.memory_BE.domain.album.entity.Album;
import com.min.i.memory_BE.domain.album.service.AlbumService;
import com.min.i.memory_BE.domain.user.security.CustomUserDetails;
import com.min.i.memory_BE.global.error.exception.EntityNotFoundException;
import com.min.i.memory_BE.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}
