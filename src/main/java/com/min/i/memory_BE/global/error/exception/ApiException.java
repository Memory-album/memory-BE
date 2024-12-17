package com.min.i.memory_BE.global.error.exception;

import com.min.i.memory_BE.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ApiException extends RuntimeException {
  private final ErrorCode errorCode;
  
  public ApiException(String message, ErrorCode errorCode) {
    super(message);
    this.errorCode = errorCode;
  }
}
