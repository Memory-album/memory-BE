package com.min.i.memory_BE.domain.album.service;

import com.min.i.memory_BE.domain.album.repository.AlbumRepository;
import com.min.i.memory_BE.global.service.S3Service;
import com.min.i.memory_BE.domain.album.dto.request.AlbumRequestDto;
import com.min.i.memory_BE.domain.album.entity.Album;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlbumService {
    
    private final AlbumRepository albumRepository;
    private final S3Service s3Service;
    
    @Autowired
    public AlbumService(AlbumRepository albumRepository, S3Service s3Service) {
        this.albumRepository = albumRepository;
        this.s3Service = s3Service;
    }
    
    public Album createAlbum(AlbumRequestDto request) {
        String thumbnailUrl = s3Service.uploadThumbnail(request.getThumbnailFile());
        
        Album album = Album.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .thumbnailUrl(thumbnailUrl)
                .theme(request.getTheme())
                .build();
        
        return albumRepository.save(album);
    }
}