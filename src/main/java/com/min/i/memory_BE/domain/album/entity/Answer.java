package com.min.i.memory_BE.domain.album.entity;

import com.min.i.memory_BE.domain.user.entity.User;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "answers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Answer extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id")
  private Question question;
  
  @Column(columnDefinition = "TEXT")
  private String voiceText;
  
  @Column(columnDefinition = "TEXT")
  private String finalStory;
  
  private boolean isPrivate = false;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by")
  private User createdBy;
  
  @Builder
  public Answer(Question question, String voiceText, String finalStory,
    boolean isPrivate, User createdBy) {
    this.question = question;
    this.voiceText = voiceText;
    this.finalStory = finalStory;
    this.isPrivate = isPrivate;
    this.createdBy = createdBy;
  }
}