package com.min.i.memory_BE.domain.album.entity;

import com.min.i.memory_BE.domain.album.enums.AlbumTheme;
import com.min.i.memory_BE.domain.album.enums.AlbumVisibility;
import com.min.i.memory_BE.domain.group.entity.Group;
import com.min.i.memory_BE.domain.media.entity.Media;
import com.min.i.memory_BE.domain.user.entity.User;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "albums")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Album extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(nullable = false)
  private String title;
  
  @Column(columnDefinition = "TEXT")
  private String description;
  
  private String thumbnailUrl;
  
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AlbumTheme theme;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id")
  private Group group;
  
  @Enumerated(EnumType.STRING)
  private AlbumVisibility visibility = AlbumVisibility.GROUP;
  
  @OneToMany(mappedBy = "album")
  private final List<AlbumPage> pages = new ArrayList<>();
  
  @OneToMany(mappedBy = "album")
  private final List<Media> mediaList = new ArrayList<>();
  
  @Builder
  public Album(String title, String description, String thumbnailUrl,
    AlbumTheme theme, User user, Group group) {
    this.title = title;
    this.description = description;
    this.thumbnailUrl = thumbnailUrl;
    this.theme = theme;
    this.user = user;
    this.group = group;
  }
}