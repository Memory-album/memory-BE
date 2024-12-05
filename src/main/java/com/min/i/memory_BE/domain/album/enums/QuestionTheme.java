package com.min.i.memory_BE.domain.album.enums;

import lombok.Getter;

@Getter
public enum QuestionTheme {
  SENIOR_CARE("시니어 케어"),
  CHILD_STORY("자녀 이야기"),
  COUPLE_STORY("커플 이야기");
  
  private final String description;
  
  QuestionTheme(String description) {
    this.description = description;
  }
}