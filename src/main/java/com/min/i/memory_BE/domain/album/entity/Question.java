package com.min.i.memory_BE.domain.album.entity;

import com.min.i.memory_BE.domain.album.enums.QuestionTheme;
import com.min.i.memory_BE.domain.media.entity.Media;
import com.min.i.memory_BE.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "media_id")
  private Media media;
  
  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;
  
  @Enumerated(EnumType.STRING)
  private QuestionTheme theme;
  
  private boolean isPrivate = false;
  
  private String keywordsUsed;
  
  private Integer level;
  
  private String category;
  
  @Builder
  public Question(Media media, String content, QuestionTheme theme,
    boolean isPrivate, String keywordsUsed, Integer level, String category) {
    this.media = media;
    this.content = content;
    this.theme = theme;
    this.isPrivate = isPrivate;
    this.keywordsUsed = keywordsUsed;
    this.level = level;
    this.category = category;
  }
  
  /**
   * 질문 내용을 수정합니다.
   * @param content 새로운 질문 내용
   */
  public void setContent(String content) {
    this.content = content;
  }
}