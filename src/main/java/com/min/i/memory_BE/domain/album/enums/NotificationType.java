package com.min.i.memory_BE.domain.album.enums;

import lombok.Getter;

@Getter
public enum NotificationType {
  NEW_STORY("새로운 스토리"),
  GROUP_INVITE("그룹 초대"),
  ALBUM_UPDATE("앨범 업데이트");
  
  private final String description;
  
  NotificationType(String description) {
    this.description = description;
  }
}
