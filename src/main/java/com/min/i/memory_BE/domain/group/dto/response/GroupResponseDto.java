package com.min.i.memory_BE.domain.group.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.min.i.memory_BE.domain.group.entity.Group;
import com.min.i.memory_BE.domain.group.entity.UserGroup;
import com.min.i.memory_BE.domain.user.enums.UserGroupRole;
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
  
  private Long userGroupId;
  private UserGroupRole role;
  private String groupNickname;
  private String groupProfileImgUrl;
  private boolean notificationEnabled;
  private Integer sortOrder;
  private LocalDateTime lastVisitAt;
  
  public static GroupResponseDto from(Group group, UserGroup userGroup) {
    return GroupResponseDto.builder()
      .id(group.getId())
      .name(group.getName())
      .groupDescription(group.getGroupDescription())
      .groupImageUrl(group.getGroupImageUrl())
      .inviteCode(group.getInviteCode())
      .createdAt(group.getCreatedAt())
      // UserGroup 정보
      .userGroupId(userGroup.getId())
      .role(userGroup.getRole())
      .groupNickname(userGroup.getGroupNickname())
      .groupProfileImgUrl(userGroup.getGroupProfileImgUrl())
      .notificationEnabled(userGroup.isNotificationEnabled())
      .sortOrder(userGroup.getSortOrder())
      .lastVisitAt(userGroup.getLastVisitAt())
      .build();
  }
}