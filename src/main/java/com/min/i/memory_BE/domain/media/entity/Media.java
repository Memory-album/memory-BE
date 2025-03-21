package com.min.i.memory_BE.domain.media.entity;

import com.min.i.memory_BE.domain.album.entity.Album;
import com.min.i.memory_BE.domain.album.entity.AlbumPage;
import com.min.i.memory_BE.domain.album.entity.Story;
import com.min.i.memory_BE.domain.media.enums.MediaType;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "media")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "image_url")
  private String imageUrl;
  
  @Column(columnDefinition = "TEXT")
  private String analysisResult;
  
  @Column(nullable = false)
  private LocalDateTime createdAt;
  
  @Column(nullable = false)
  private String fileUrl;
  
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MediaType fileType;
  
  @Column(nullable = false)
  private String originalFilename;
  
  @Column(nullable = false)
  private Long fileSize;
  
  @Column(columnDefinition = "TEXT")
  private String metadata;
  
  private String thumbnailUrl;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "album_id")
  private Album album;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "page_id")
  private AlbumPage page;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "uploaded_by")
  private User uploadedBy;
  
  @OneToMany(mappedBy = "media", cascade = CascadeType.ALL)
  private final List<MediaKeyword> mediaKeywords = new ArrayList<>();

  @OneToMany(mappedBy = "media", cascade = CascadeType.ALL)
  private final List<Story> stories = new ArrayList<>();
  
  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }
  
  public void setFileUrl(String fileUrl) {
    this.fileUrl = fileUrl;
  }
  
  public void setFileType(MediaType fileType) {
    this.fileType = fileType;
  }
  
  public void setOriginalFilename(String originalFilename) {
    this.originalFilename = originalFilename;
  }
  
  public void setFileSize(Long fileSize) {
    this.fileSize = fileSize;
  }
  
  /**
   * AI 분석 결과를 저장합니다.
   * @param analysisResultJson AI 서버에서 분석한 결과 JSON
   */
  public void setAnalysisResult(String analysisResultJson) {
    this.analysisResult = analysisResultJson;
    this.metadata = analysisResultJson;
  }
  
  /**
   * 미디어에 키워드를 추가합니다.
   * @param mediaKeyword 미디어 키워드 관계 엔티티
   */
  public void addMediaKeyword(MediaKeyword mediaKeyword) {
    this.mediaKeywords.add(mediaKeyword);
  }
}