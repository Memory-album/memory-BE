package com.min.i.memory_BE.global.error.exception;

import com.min.i.memory_BE.global.error.ErrorCode;

public class S3Exception extends ApiException {
  public S3Exception(String message) {
    super(message, ErrorCode.INTERNAL_SERVER_ERROR);
  }
}
