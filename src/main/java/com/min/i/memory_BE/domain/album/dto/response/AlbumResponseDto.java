package com.min.i.memory_BE.domain.album.dto.response;

import com.min.i.memory_BE.domain.album.entity.Album;
import com.min.i.memory_BE.domain.album.enums.AlbumTheme;
import com.min.i.memory_BE.domain.album.enums.AlbumVisibility;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AlbumResponseDto {
    private Long id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private AlbumTheme theme;
    private AlbumVisibility visibility;
    private Long userId;
    private Long groupId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AlbumResponseDto fromEntity(Album album) {
        return AlbumResponseDto.builder()
                .id(album.getId())
                .title(album.getTitle())
                .description(album.getDescription())
                .thumbnailUrl(album.getThumbnailUrl())
                .theme(album.getTheme())
                .visibility(album.getVisibility())
                .userId(album.getUser() != null ? album.getUser().getId() : null)
                .groupId(album.getGroup() != null ? album.getGroup().getId() : null)
                .createdAt(album.getCreatedAt())
                .updatedAt(album.getUpdatedAt())
                .build();
    }
} 