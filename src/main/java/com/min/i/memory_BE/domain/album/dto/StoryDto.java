package com.min.i.memory_BE.domain.album.dto;

import com.min.i.memory_BE.domain.album.entity.Story;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 스토리 데이터 전송 객체
 */
@NoArgsConstructor
public class StoryDto {

    /**
     * 스토리 수정 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Update {
        @NotBlank(message = "스토리 내용은 필수입니다.")
        private String content;

        @Builder
        public Update(String content) {
            this.content = content;
        }
    }
    
    /**
     * 스토리 응답 DTO
     */
    @Getter
    @Builder
    public static class Response {
        private Long id;
        private Long mediaId;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        /**
         * Story 엔티티를 Response DTO로 변환
         */
        public static Response fromEntity(Story story) {
            return Response.builder()
                .id(story.getId())
                .mediaId(story.getMedia() != null ? story.getMedia().getId() : null)
                .content(story.getContent())
                .createdAt(story.getCreatedAt())
                .updatedAt(story.getUpdatedAt())
                .build();
        }
    }
} 