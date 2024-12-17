package com.min.i.memory_BE.global.error;

import com.min.i.memory_BE.global.error.exception.ApiException;
import com.min.i.memory_BE.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
  
  @ExceptionHandler(ApiException.class)
  protected ResponseEntity<ApiResponse<?>> handleApiException(ApiException e) {
    ErrorCode errorCode = e.getErrorCode();
    logError(errorCode, e.getMessage());
    return ResponseEntity
      .status(errorCode.getStatus())
      .body(ApiResponse.error(errorCode));
  }
  
  private void logError(ErrorCode errorCode, String message) {
    switch (errorCode.getStatus()) {
      case 500 -> log.error(message);
      case 400, 404 -> log.warn(message);
      default -> log.info(message);
    }
  }
}
