package com.min.i.memory_BE.global.error;

import com.min.i.memory_BE.global.error.exception.ApiException;
import com.min.i.memory_BE.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import java.util.Map;

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
  
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException e) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
      .body(Map.of(
        "message", "접근 권한이 없습니다.",
        "status", "error"
      ));
  }
  
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<?> handleAuthenticationException(AuthenticationException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .body(Map.of(
        "message", "로그인이 필요한 서비스입니다.",
        "status", "error"
      ));
  }
  
  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleAllException(Exception e) {
    log.error("Unexpected error occurred: ", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(Map.of(
        "message", "서버 오류가 발생했습니다.",
        "status", "error"
      ));
  }
  
  private void logError(ErrorCode errorCode, String message) {
    switch (errorCode.getStatus()) {
      case 500 -> log.error(message);
      case 400, 404 -> log.warn(message);
      default -> log.info(message);
    }
  }
}
