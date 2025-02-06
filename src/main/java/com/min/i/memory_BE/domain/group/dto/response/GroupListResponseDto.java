package com.min.i.memory_BE.domain.group.dto.response;

import com.min.i.memory_BE.domain.group.entity.Group;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupListResponseDto {
  private Long id;
  private String name;
  private String groupImageUrl;
  
  public static GroupListResponseDto from(Group group) {
    return GroupListResponseDto.builder()
      .id(group.getId())
      .name(group.getName())
      .groupImageUrl(group.getGroupImageUrl())
      .build();
  }
}