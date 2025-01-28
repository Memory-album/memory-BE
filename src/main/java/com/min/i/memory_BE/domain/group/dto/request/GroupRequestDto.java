package com.min.i.memory_BE.domain.group.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

public class GroupRequestDto {
  
  @Getter @Builder
  public static class Create {
    @NotBlank(message = "그룹 이름은 필수입니다")
    private String name;
    
    private String groupDescription;
  }
  
  @Getter @Builder
  public static class Update {
    @NotBlank(message = "그룹 이름은 필수입니다")
    private String name;
    
    private String groupDescription;
  }
}
