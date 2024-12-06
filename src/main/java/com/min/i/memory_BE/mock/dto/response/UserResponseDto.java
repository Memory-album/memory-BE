package com.min.i.memory_BE.mock.dto.response;

import com.min.i.memory_BE.domain.user.enums.UserGroupRole;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class UserResponseDto {
  private Long id;
  private String email;
  private String name;
  private String profileImgUrl;
  private UserGroupRole role;
  private Integer unreadAnswers;
  private List<GroupSimpleDto> groups;
  
  @Getter @Builder
  public static class GroupSimpleDto {
    private Long id;
    private String name;
    private String groupImageUrl;
  }
}