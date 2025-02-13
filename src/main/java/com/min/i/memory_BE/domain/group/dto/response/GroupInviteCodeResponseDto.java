package com.min.i.memory_BE.domain.group.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class GroupInviteCodeResponseDto {
  private String inviteCode;
  private LocalDateTime expiryAt;
  private boolean isActive;
  
  public static GroupInviteCodeResponseDto from(String inviteCode, LocalDateTime expiryAt, boolean isActive) {
    return GroupInviteCodeResponseDto.builder()
      .inviteCode(inviteCode)
      .expiryAt(expiryAt)
      .isActive(isActive)
      .build();
  }
}