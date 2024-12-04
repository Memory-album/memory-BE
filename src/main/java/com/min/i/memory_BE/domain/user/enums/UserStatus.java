package com.min.i.memory_BE.domain.user.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
  ACTIVE("활성"),
  INACTIVE("비활성"),
  DELETED("삭제됨");
  
  private final String description;
  
  UserStatus(String description) {
    this.description = description;
  }
}
