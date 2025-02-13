package com.min.i.memory_BE.domain.group.dto.response;

import com.min.i.memory_BE.domain.group.entity.Group;
import com.min.i.memory_BE.domain.group.entity.UserGroup;
import com.min.i.memory_BE.domain.user.enums.UserGroupRole;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupDetailResponseDto {
    private Long id;
    private String name;
    private String groupDescription;
    private String groupImageUrl;
    private LocalDateTime createdAt;
    private Long userGroupId;
    private UserGroupRole role;
    private String userName;
    private String groupProfileImgUrl;
    private boolean notificationEnabled;
    private Integer sortOrder;
    private LocalDateTime lastVisitAt;
    private String ownerName;

    public static GroupDetailResponseDto from(Group group, UserGroup myUserGroup, UserGroup ownerUserGroup) {
        return GroupDetailResponseDto.builder()
            .id(group.getId())
            .name(group.getName())
            .groupDescription(group.getGroupDescription())
            .groupImageUrl(group.getGroupImageUrl())
            .createdAt(group.getCreatedAt())
            .userGroupId(myUserGroup.getId())
            .role(myUserGroup.getRole())
            .userName(myUserGroup.getUser().getName())
            .groupProfileImgUrl(myUserGroup.getGroupProfileImgUrl())
            .notificationEnabled(myUserGroup.isNotificationEnabled())
            .sortOrder(myUserGroup.getSortOrder())
            .lastVisitAt(myUserGroup.getLastVisitAt())
            .ownerName(ownerUserGroup.getUser().getName())
            .build();
    }
} 