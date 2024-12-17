package com.min.i.memory_BE.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  // Common
  INVALID_INPUT_VALUE(400, "C001", "잘못된 입력값입니다"),
  ENTITY_NOT_FOUND(404, "C002", "리소스를 찾을 수 없습니다"),
  INTERNAL_SERVER_ERROR(500, "C003", "서버 내부 오류가 발생했습니다");
  
  private final int status;
  private final String code;
  private final String message;
}
