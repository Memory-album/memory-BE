package com.min.i.memory_BE.domain.album.entity;

import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.media.entity.Media;
import com.min.i.memory_BE.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "answers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Answer extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "media_id", nullable = false)
  private Media media;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
  
  @Column(nullable = false)
  private String content;

  @Column(nullable = false)
  @Builder.Default
  private boolean isPrivate = false;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;
  
  @Builder
  public Answer(Media media, User user, String content,
    boolean isPrivate, Question question) {
    this.media = media;
    this.user = user;
    this.content = content;
    this.isPrivate = isPrivate;
    this.question = question;
  }
}