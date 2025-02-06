package com.min.i.memory_BE.domain.album.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
public class AlbumRequestDto {
    @NotBlank(message = "앨범 제목은 필수입니다.")
    private String title;

    private String description;

    private MultipartFile thumbnailFile;

    @NotNull(message = "앨범 테마는 필수입니다.")
    private String theme;

    private Long userId;
    
    private Long groupId;
}