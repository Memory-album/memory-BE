package com.min.i.memory_BE.domain.album.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class AnswerRequest {
    private Long questionId;
    private String textContent;
    private MultipartFile audioFile;
} 