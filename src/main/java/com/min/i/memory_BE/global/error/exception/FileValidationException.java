package com.min.i.memory_BE.global.error.exception;

import com.min.i.memory_BE.global.error.ErrorCode;

public class FileValidationException extends ApiException {
  public FileValidationException(String message) {
    super(message, ErrorCode.INVALID_INPUT_VALUE);
  }
}