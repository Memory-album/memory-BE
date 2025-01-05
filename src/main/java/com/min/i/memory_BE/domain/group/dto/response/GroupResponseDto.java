package com.min.i.memory_BE.domain.group.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.min.i.memory_BE.domain.group.entity.Group;
import com.min.i.memory_BE.mock.dto.response.AlbumResponseDto.UserSimpleDto;
import java.time.LocalDateTime;
import java.util.List;
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
  private LocalDateTime createdAt;
  private UserSimpleDto owner;
  private List<UserSimpleDto> members;
  
  
  public static GroupResponseDto from(Group entity) {
    return GroupResponseDto.builder()
      .id(entity.getId())
      .name(entity.getName())
      .groupDescription(entity.getGroupDescription())
      .groupImageUrl(entity.getGroupImageUrl())
      .createdAt(entity.getCreatedAt())
      .build();
  }
  
  public Group toEntity() {
    return Group.builder()
      .name(this.name)
      .groupDescription(this.groupDescription)
      .groupImageUrl(this.groupImageUrl)
      .build();
  }
}