package com.min.i.memory_BE.domain.album.enums;

import lombok.Getter;

@Getter
public enum AlbumVisibility {
  PUBLIC("전체 공개"),
  GROUP("그룹 공개"),
  PRIVATE("비공개");
  
  private final String description;
  
  AlbumVisibility(String description) {
    this.description = description;
  }
}

