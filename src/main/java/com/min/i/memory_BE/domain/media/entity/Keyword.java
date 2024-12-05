package com.min.i.memory_BE.domain.media.entity;

import com.min.i.memory_BE.domain.media.enums.KeywordCategory;
import com.min.i.memory_BE.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "keywords")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Keyword extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(nullable = false, unique = true)
  private String name;
  
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private KeywordCategory category;
  
  @OneToMany(mappedBy = "keyword")
  private final List<MediaKeyword> mediaKeywords = new ArrayList<>();
  
  @Builder
  public Keyword(String name, KeywordCategory category) {
    this.name = name;
    this.category = category;
  }
}