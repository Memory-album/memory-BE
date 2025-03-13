package com.min.i.memory_BE.domain.album.controller;

import com.min.i.memory_BE.domain.album.dto.request.AlbumRequestDto;
import com.min.i.memory_BE.domain.album.entity.Album;
import com.min.i.memory_BE.domain.album.service.AlbumService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/albums")
@Tag(name = "Album API", description = "앨범 관리 API")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Album> createAlbum(@ModelAttribute AlbumRequestDto request) {
        Album album = albumService.createAlbum(request);
        return ResponseEntity.ok(album);
    }
}
