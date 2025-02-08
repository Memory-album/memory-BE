package com.min.i.memory_BE.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  // Common
  INVALID_INPUT_VALUE(400, "C001", "잘못된 입력값입니다"),
  ENTITY_NOT_FOUND(404, "C002", "리소스를 찾을 수 없습니다"),
  INTERNAL_SERVER_ERROR(500, "C003", "서버 내부 오류가 발생했습니다"),
  
  // Group
  GROUP_NOT_FOUND(404, "G001", "그룹을 찾을 수 없습니다"),
  NOT_GROUP_MEMBER(400, "G002", "해당 그룹의 멤버가 아닙니다"),
  OWNER_CANNOT_LEAVE(400, "G003", "그룹 소유자는 먼저 다른 멤버에게 소유권을 이전해야 합니다"),
  GROUP_MEMBER_NOT_FOUND(404, "G004", "그룹 멤버를 찾을 수 없습니다"),
  INVALID_GROUP_OPERATION(400, "G005", "잘못된 그룹 작업입니다");
  
  private final int status;
  private final String code;
  private final String message;
}
