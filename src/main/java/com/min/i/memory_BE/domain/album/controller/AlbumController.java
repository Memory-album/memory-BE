package com.min.i.memory_BE.domain.album.controller;

import com.min.i.memory_BE.domain.album.entity.Album;
import com.min.i.memory_BE.domain.album.enums.AlbumTheme;
import com.min.i.memory_BE.domain.album.service.AlbumService;
import com.min.i.memory_BE.domain.album.dto.request.AlbumRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/albums")
public class AlbumController {
    
    private final AlbumService albumService;
    
    @Autowired
    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }
    
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Album> createAlbum(
            @RequestPart("title") String title,
            @RequestPart("description") String description,
            @RequestPart("thumbnailFile") MultipartFile thumbnailFile,
            @RequestPart("theme") String theme,
            @RequestPart("userId") Long userId,
            @RequestPart("groupId") Long groupId) {
        
        AlbumRequestDto request = AlbumRequestDto.builder()
                .title(title)
                .description(description)
                .thumbnailFile(thumbnailFile)
                .theme(theme)
                .userId(userId)
                .groupId(groupId)
                .build();
        
        Album album = albumService.createAlbum(request);
        return ResponseEntity.ok(album);
    }
}