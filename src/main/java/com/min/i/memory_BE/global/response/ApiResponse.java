package com.min.i.memory_BE.global.response;

import com.min.i.memory_BE.global.error.ErrorCode;
import com.min.i.memory_BE.global.error.ErrorMessage;
import lombok.Getter;

@Getter
public class ApiResponse<T> {
  private final ResultType result;
  private final ErrorMessage message;
  private final T data;
  
  private ApiResponse(ResultType result, ErrorMessage message, T data) {
    this.result = result;
    this.message = message;
    this.data = data;
  }
  
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(ResultType.SUCCESS, null, data);
  }
  
  public static ApiResponse<?> error(ErrorCode errorCode) {
    return new ApiResponse<>(ResultType.ERROR, new ErrorMessage(errorCode), null);
  }
}
