package com.min.i.memory_BE.domain.media.entity;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "media_keywords",
  uniqueConstraints = {
    @UniqueConstraint(columnNames = {"media_id", "keyword_id"})
  })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MediaKeyword extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "media_id")
  private Media media;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "keyword_id")
  private Keyword keyword;
  
  @Column(nullable = false)
  private Float confidenceScore;
  
  @Builder
  public MediaKeyword(Media media, Keyword keyword, Float confidenceScore) {
    this.media = media;
    this.keyword = keyword;
    this.confidenceScore = confidenceScore;
  }
}