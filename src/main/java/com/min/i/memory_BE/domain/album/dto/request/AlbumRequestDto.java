package com.min.i.memory_BE.domain.album.dto.request;

import com.min.i.memory_BE.domain.album.enums.AlbumTheme;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class AlbumRequestDto {
    @NotBlank(message = "앨범 제목은 필수입니다.")
    private String title;

    private String description;

    private MultipartFile thumbnailFile;

    @NotNull(message = "앨범 테마는 필수입니다.")
    private AlbumTheme theme;

    private Long userId;

    private Long groupId;
}
