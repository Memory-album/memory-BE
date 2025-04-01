package com.min.i.memory_BE.domain.media.dto.response;

import com.min.i.memory_BE.domain.media.entity.Media;
import com.min.i.memory_BE.domain.media.enums.MediaType;
import com.min.i.memory_BE.domain.user.dto.UserSimpleDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
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
    private String story;   // Story 필드 (null 허용)

    /**
     * 미디어 엔티티로부터 응답 DTO를 생성합니다.
     * Story 필드는 우선 null로 설정합니다.
     */
    public static MediaResponseDto from(Media media) {
        return MediaResponseDto.builder()
                .id(media.getId())
                .fileUrl(media.getFileUrl())
                .fileType(media.getFileType())
                .originalFilename(media.getOriginalFilename())
                .fileSize(media.getFileSize())
                .thumbnailUrl(media.getThumbnailUrl())
                .uploadedBy(UserSimpleDto.fromMedia(media))
                .createdAt(media.getCreatedAt())
                .story(null)  // Story 필드는 우선 null로 설정
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