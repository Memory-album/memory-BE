package com.min.i.memory_BE.domain.group.dto.response;

import com.min.i.memory_BE.domain.group.entity.UserGroup;
import com.min.i.memory_BE.domain.user.enums.UserGroupRole;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class GroupMemberResponseDto {
  private Long id;
  private String name;
  private String profileImgUrl;
  private LocalDateTime joinedAt;
  
  public static GroupMemberResponseDto from(UserGroup userGroup) {
    return GroupMemberResponseDto.builder()
      .id(userGroup.getUser().getId())
      .name(userGroup.getUser().getName())
      .profileImgUrl(userGroup.getGroupProfileImgUrl())
      .joinedAt(userGroup.getCreatedAt())
      .build();
  }
}