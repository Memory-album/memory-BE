package com.min.i.memory_BE.global.error;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ErrorResponse {
  private String message;
  private String code;
  
  private ErrorResponse(ErrorCode errorCode) {
    this.message = errorCode.getMessage();
    this.code = errorCode.getCode();
  }
  
  private ErrorResponse(ErrorCode errorCode, String message) {
    this.message = message;
    this.code = errorCode.getCode();
  }
  
  public static ErrorResponse of(ErrorCode errorCode) {
    return new ErrorResponse(errorCode);
  }
  
  public static ErrorResponse of(ErrorCode errorCode, String message) {
    return new ErrorResponse(errorCode, message);
  }
}
