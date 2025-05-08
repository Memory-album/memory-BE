package com.min.i.memory_BE.domain.media.dto.response;

import com.min.i.memory_BE.domain.album.entity.Story;
import com.min.i.memory_BE.domain.media.entity.Media;
import com.min.i.memory_BE.domain.media.enums.MediaType;
import com.min.i.memory_BE.domain.user.dto.UserSimpleDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Builder
public class MediaResponseDto {
    private Long id;
    private String fileUrl;
    private MediaType fileType;
    private String originalFilename;
    private Long fileSize;
    private String thumbnailUrl;
    private UserSimpleDto uploadedBy;
    private LocalDateTime createdAt;
    private String story;   // Story 필드

    /**
     * 미디어 엔티티로부터 응답 DTO를 생성합니다.
     */
    public static MediaResponseDto from(Media media) {
        // 스토리 정보 가져오기
        String storyContent = null;
        if (!media.getStories().isEmpty()) {
            // 미디어에 연결된 첫 번째 스토리의 내용을 가져옴
            storyContent = media.getStories().get(0).getContent();
        }
        
        return MediaResponseDto.builder()
                .id(media.getId())
                .fileUrl(media.getFileUrl())
                .fileType(media.getFileType())
                .originalFilename(media.getOriginalFilename())
                .fileSize(media.getFileSize())
                .thumbnailUrl(media.getThumbnailUrl())
                .uploadedBy(UserSimpleDto.fromMedia(media))
                .createdAt(media.getCreatedAt())
                .story(storyContent)  
                .build();
    }

    /**
     * 미디어 엔티티 목록을 응답 DTO 목록으로 변환합니다.
     */
    public static List<MediaResponseDto> fromList(List<Media> mediaList) {
        if (mediaList == null) return List.of();

        return mediaList.stream()
                .map(MediaResponseDto::from)
                .collect(Collectors.toList());
    }
}