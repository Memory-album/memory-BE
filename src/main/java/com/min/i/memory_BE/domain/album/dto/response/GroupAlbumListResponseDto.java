package com.min.i.memory_BE.domain.album.dto.response;

import com.min.i.memory_BE.domain.album.entity.Album;
import com.min.i.memory_BE.domain.media.dto.response.MediaResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class GroupAlbumListResponseDto {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MediaResponseDto> recentMedia; // 최근 미디어 목록 (썸네일용)

    /**
     * 앨범 엔티티와 최근 미디어 목록을 이용하여 DTO 객체를 생성합니다.
     *
     * @param album 앨범 엔티티
     * @param mediaList 최근 미디어 목록
     * @return 그룹 앨범 리스트 응답 DTO
     */
    public static GroupAlbumListResponseDto from(Album album, List<MediaResponseDto> mediaList) {
        return GroupAlbumListResponseDto.builder()
                .id(album.getId())
                .title(album.getTitle())
                .description(album.getDescription())
                .createdAt(album.getCreatedAt())
                .updatedAt(album.getUpdatedAt())
                .recentMedia(mediaList)
                .build();
    }
} 