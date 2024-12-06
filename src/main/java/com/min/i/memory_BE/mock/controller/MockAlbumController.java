package com.min.i.memory_BE.mock.controller;

import com.min.i.memory_BE.mock.data.MockAlbumData;
import com.min.i.memory_BE.mock.dto.response.AlbumResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Mock Album API")
@RestController
@RequestMapping("/api/v1/mock/albums")
@RequiredArgsConstructor
public class MockAlbumController {
  private final MockAlbumData mockAlbumData;
  
  @Operation(summary = "앨범 목록 조회")
  @GetMapping
  public ResponseEntity<List<AlbumResponseDto>> getAlbums() {
    return ResponseEntity.ok(mockAlbumData.getMockAlbums());
  }
  
  @Operation(summary = "앨범 상세 조회")
  @GetMapping("/{albumId}")
  public ResponseEntity<AlbumResponseDto> getAlbum(
    @Parameter(description = "앨범 ID") @PathVariable Long albumId
  ) {
    return ResponseEntity.ok(mockAlbumData.getMockAlbums().get(0));
  }
}