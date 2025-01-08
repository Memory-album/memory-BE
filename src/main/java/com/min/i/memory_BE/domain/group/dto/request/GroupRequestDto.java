package com.min.i.memory_BE.domain.group.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupRequestDto {
  @NotBlank(message = "그룹 이름은 필수입니다")
  private String name;
  
  private String groupDescription;
  private String groupImageUrl;
}
