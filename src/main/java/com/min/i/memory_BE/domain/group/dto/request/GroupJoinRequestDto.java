package com.min.i.memory_BE.domain.group.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupJoinRequestDto {
  @NotBlank(message = "초대 코드는 필수랍니다 ㅎ")
  
  private String inviteCode;
  private String groupNickname;
  
}
