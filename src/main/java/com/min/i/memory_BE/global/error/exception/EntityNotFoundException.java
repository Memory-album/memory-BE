package com.min.i.memory_BE.global.error.exception;

import com.min.i.memory_BE.global.error.ErrorCode;

public class EntityNotFoundException extends ApiException {
  public EntityNotFoundException(String message) {
    super(message, ErrorCode.ENTITY_NOT_FOUND);
  }
}
