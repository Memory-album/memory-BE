package com.min.i.memory_BE.domain.media.controller;

import com.min.i.memory_BE.domain.media.entity.Media;
import com.min.i.memory_BE.domain.media.service.MediaService;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Media API", description = "미디어 관리 API")
@RestController
@RequestMapping("/api/v1/groups/{groupId}/albums/{albumId}/media")
@RequiredArgsConstructor
public class MediaController {
  private final MediaService mediaService;
  
  @Operation(
    summary = "미디어 업로드",
    description = "앨범에 새로운 미디어를 업로드합니다."
  )
  @ApiResponses(value = {
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "200",
      description = "업로드 성공",
      content = @Content(schema = @Schema(implementation = Media.class))
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "400",
      description = "잘못된 요청"
    )
  })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<Media>> uploadMedia(
    @Parameter(description = "그룹 ID") @PathVariable Long groupId,
    @Parameter(description = "앨범 ID") @PathVariable Long albumId,
    @Parameter(description = "업로드할 파일") @RequestParam("file") MultipartFile file,
    @AuthenticationPrincipal User user) {
    Media media = mediaService.uploadMedia(file, groupId, albumId, user);
    return ResponseEntity.ok(ApiResponse.success(media));
  }
  
  @Operation(summary = "앨범 미디어 목록 조회", description = "특정 앨범의 미디어 목록을 조회합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse<Page<Media>>> getAlbumMedia(
    @Parameter(description = "그룹 ID") @PathVariable Long groupId,
    @Parameter(description = "앨범 ID") @PathVariable Long albumId,
    @Parameter(description = "페이징 정보") @PageableDefault(size = 20) Pageable pageable) {
    Page<Media> mediaPage = mediaService.getAlbumMedia(groupId, albumId, pageable);
    return ResponseEntity.ok(ApiResponse.success(mediaPage));
  }
  
  @Operation(summary = "미디어 수정", description = "기존 미디어를 새로운 파일로 수정합니다.")
  @PutMapping(value = "/{mediaId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<Media>> updateMedia(
    @Parameter(description = "그룹 ID") @PathVariable Long groupId,
    @Parameter(description = "앨범 ID") @PathVariable Long albumId,
    @Parameter(description = "미디어 ID") @PathVariable Long mediaId,
    @Parameter(description = "새로운 파일") @RequestParam("file") MultipartFile file,
    @AuthenticationPrincipal User user) {
    Media media = mediaService.updateMedia(groupId, albumId, mediaId, file, user);
    return ResponseEntity.ok(ApiResponse.success(media));
  }
  
  @Operation(summary = "미디어 삭제", description = "미디어를 삭제합니다.")
  @DeleteMapping("/{mediaId}")
  public ResponseEntity<ApiResponse<Void>> deleteMedia(
    @Parameter(description = "그룹 ID") @PathVariable Long groupId,
    @Parameter(description = "앨범 ID") @PathVariable Long albumId,
    @Parameter(description = "미디어 ID") @PathVariable Long mediaId,
    @AuthenticationPrincipal User user) {
    mediaService.deleteMedia(groupId, albumId, mediaId, user);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}