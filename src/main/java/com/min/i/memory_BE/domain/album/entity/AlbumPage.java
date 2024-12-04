package com.min.i.memory_BE.domain.album.entity;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "album_pages",
  uniqueConstraints = {
    @UniqueConstraint(columnNames = {"album_id", "page_number"})
  })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlbumPage extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "album_id")
  private Album album;
  
  @Column(nullable = false)
  private Integer pageNumber;
  
  @Column(nullable = false)
  private String layoutType;
  
  @OneToMany(mappedBy = "page")
  private final List<Media> mediaList = new ArrayList<>();
  
  @Builder
  public AlbumPage(Album album, Integer pageNumber, String layoutType) {
    this.album = album;
    this.pageNumber = pageNumber;
    this.layoutType = layoutType;
  }
}
