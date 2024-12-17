package com.min.i.memory_BE.domain.album.enums;

import lombok.Getter;

@Getter
public enum AlbumTheme {
  SENIOR_CARE("시니어 케어", "가족과 함께하는 소중한 순간들을 기록하고 공유하세요. 어르신들의 일상과 특별한 순간들을 담아내는 공간입니다."),
  CHILD_GROWTH("자녀 성장", "아이의 성장과정을 기록하고 가족들과 공유하세요. 첫 걸음마부터 특별한 순간까지, 모든 소중한 기억을 담을 수 있습니다."),
  COUPLE_STORY("커플 스토리", "연인과의 특별한 순간들을 기록하고 추억하세요. 데이트, 기념일 등 둘만의 소중한 이야기를 담는 공간입니다.");
  
  private final String themeName;
  private final String themeDetail;
  
  AlbumTheme(String themeName, String themeDetail) {
    this.themeName = themeName;
    this.themeDetail = themeDetail;
  }
}