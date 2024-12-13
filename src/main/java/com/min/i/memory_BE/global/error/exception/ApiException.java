package com.min.i.memory_BE.global.error.exception;

import com.min.i.memory_BE.global.error.ErrorCode;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
  private final ErrorCode errorCode;
  
  public ApiException(String message, ErrorCode errorCode) {
    super(message);
    this.errorCode = errorCode;
  }
  
  public ApiException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
