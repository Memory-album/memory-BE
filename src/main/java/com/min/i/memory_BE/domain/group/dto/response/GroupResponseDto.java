package com.min.i.memory_BE.domain.group.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.min.i.memory_BE.domain.group.entity.Group;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupResponseDto {
  private Long id;
  private String name;
  private String groupDescription;
  private String groupImageUrl;
  private String inviteCode;
  private LocalDateTime createdAt;
  private boolean isInviteCodeActive;
  private LocalDateTime inviteCodeExpiryAt;
  
  public static GroupResponseDto from(Group group) {
    return GroupResponseDto.builder()
      .id(group.getId())
      .name(group.getName())
      .groupDescription(group.getGroupDescription())
      .groupImageUrl(group.getGroupImageUrl())
      .inviteCode(group.getInviteCode())
      .createdAt(group.getCreatedAt())
      .isInviteCodeActive(group.isInviteCodeActive())
      .inviteCodeExpiryAt(group.getInviteCodeExpiryAt())
      .build();
  }
}