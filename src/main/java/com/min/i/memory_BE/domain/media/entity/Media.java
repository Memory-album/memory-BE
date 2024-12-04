package com.min.i.memory_BE.domain.media.entity;

import com.min.i.memory_BE.domain.album.entity.Album;
import com.min.i.memory_BE.domain.album.entity.AlbumPage;
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
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "media")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Media extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(nullable = false)
  private String fileUrl;
  
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MediaType fileType;
  
  @Column(nullable = false)
  private String originalFilename;
  
  @Column(nullable = false)
  private Long fileSize;
  
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
  private List<MediaKeyword> mediaKeywords = new ArrayList<>();
  
  @Builder
  public Media(String fileUrl, MediaType fileType, String originalFilename, Long fileSize,
    String metadata, String thumbnailUrl, Album album, AlbumPage page, User uploadedBy) {
    this.fileUrl = fileUrl;
    this.fileType = fileType;
    this.originalFilename = originalFilename;
    this.fileSize = fileSize;
    this.metadata = metadata;
    this.thumbnailUrl = thumbnailUrl;
    this.album = album;
    this.page = page;
    this.uploadedBy = uploadedBy;
  }
}