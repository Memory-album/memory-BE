package com.min.i.memory_BE.domain.album.enums;

import lombok.Getter;

@Getter
public enum AlbumTheme {
  SENIOR_CARE("시니어 케어"),
  CHILD_GROWTH("자녀 성장"),
  COUPLE_STORY("커플 스토리");
  
  private final String description;
  
  AlbumTheme(String description) {
    this.description = description;
  }
}
