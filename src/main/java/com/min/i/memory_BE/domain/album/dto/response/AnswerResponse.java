package com.min.i.memory_BE.domain.album.dto.response;

import com.min.i.memory_BE.domain.album.entity.Answer;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class AnswerResponse {
    private Long id;
    private Long questionId;
    private Long userId;
    private String content;
    private boolean isPrivate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userName;  // 답변 작성자 이름
    private String questionContent;  // 질문 내용

    public static AnswerResponse from(Answer answer) {
        return AnswerResponse.builder()
                .id(answer.getId())
                .questionId(answer.getQuestion().getId())
                .userId(answer.getUser().getId())
                .content(answer.getContent())
                .isPrivate(answer.isPrivate())
                .createdAt(answer.getCreatedAt())
                .updatedAt(answer.getUpdatedAt())
                .userName(answer.getUser().getName())
                .questionContent(answer.getQuestion().getContent())
                .build();
    }
}