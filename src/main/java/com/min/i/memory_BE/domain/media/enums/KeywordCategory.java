package com.min.i.memory_BE.domain.media.enums;

import lombok.Getter;

@Getter
public enum KeywordCategory {
  OBJECT("물체"),
  EMOTION("감정"),
  ACTION("행동"),
  PLACE("장소"),
  EVENT("이벤트");
  
  private final String description;
  
  KeywordCategory(String description) {
    this.description = description;
  }
}