package com.min.i.memory_BE.domain.group.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupJoinResponseDto {
  private Long groupId;
  private String message;
  
  public static GroupJoinResponseDto of(Long groupId) {
    return GroupJoinResponseDto.builder()
      .groupId(groupId)
      .message("그룹 참여가 완료됐슴다 개꿀")
      .build();
  }
}