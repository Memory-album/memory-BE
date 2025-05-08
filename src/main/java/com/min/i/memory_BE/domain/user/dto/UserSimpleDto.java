package com.min.i.memory_BE.domain.user.dto;

import com.min.i.memory_BE.domain.media.entity.Media;
import com.min.i.memory_BE.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSimpleDto {
    private Long id;
    private String name;
    private String profileImgUrl;

    public static UserSimpleDto from(User user) {
        if (user == null) return null;

        return UserSimpleDto.builder()
                .id(user.getId())
                .name(user.getName())
                .profileImgUrl(user.getProfileImgUrl())
                .build();
    }

    public static UserSimpleDto fromMedia(Media media) {
        if (media == null || media.getUploadedBy() == null) return null;
        return from(media.getUploadedBy());
    }
}