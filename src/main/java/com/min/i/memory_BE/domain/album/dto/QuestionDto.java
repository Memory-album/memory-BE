package com.min.i.memory_BE.domain.album.dto;

import com.min.i.memory_BE.domain.album.entity.Question;
import com.min.i.memory_BE.domain.album.enums.QuestionTheme;
import com.min.i.memory_BE.domain.user.dto.UserSimpleDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 질문 데이터 전송 객체
 */
@Getter
@Setter
@NoArgsConstructor
public class QuestionDto {
    
    /**
     * 사용자 직접 입력 질문 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Create {
        @NotNull(message = "미디어 ID는 필수입니다.")
        private Long mediaId;
        
        @NotBlank(message = "질문 내용은 필수입니다.")
        private String content;

        @Builder
        public Create(Long mediaId, String content) {
            this.mediaId = mediaId;
            this.content = content;
        }
    }
    
    /**
     * 질문 수정 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Update {
        @NotBlank(message = "질문 내용은 필수입니다.")
        private String content;

        @Builder
        public Update(String content) {
            this.content = content;
        }
    }
    
    /**
     * 질문 응답 DTO
     */
    @Getter
    @Builder
    public static class Response {
        private Long id;
        private Long mediaId;
        private String content;
        private QuestionTheme theme;
        private String category;
        private Integer level;
        private Boolean isPrivate;
        private UserSimpleDto uploader;
        
        /**
         * Question 엔티티를 Response DTO로 변환
         */
        public static Response fromEntity(Question question) {
            return Response.builder()
                .id(question.getId())
                .mediaId(question.getMedia() != null ? question.getMedia().getId() : null)
                .content(question.getContent())
                .theme(question.getTheme())
                .category(question.getCategory())
                .level(question.getLevel())
                .isPrivate(question.isPrivate())
                .uploader(question.getMedia() != null && question.getMedia().getUploadedBy() != null ? 
                          UserSimpleDto.from(question.getMedia().getUploadedBy()) : null)
                .build();
        }
    }
} 