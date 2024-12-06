package com.min.i.memory_BE.domain.user.enums;

import lombok.Getter;

@Getter
public enum UserGroupRole {
  OWNER("엘범생성자"),
  MEMBER("멤버"),
  SENIOR("어르신");
  
  private final String description;
  
  UserGroupRole(String description) {
    this.description = description;
  }
}
