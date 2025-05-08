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

  /**
   * 앨범의 그룹을 설정합니다.
   * @param group 연결할 그룹
   * @return 업데이트된 앨범 인스턴스
   */
  public Album updateGroup(Group group) {
    this.group = group;
    return this;
  }
  
  /**
   * 앨범의 소유자를 설정합니다.
   * @param user 앨범의 소유자
   * @return 업데이트된 앨범 인스턴스
   */
  public Album updateUser(User user) {
    this.user = user;
    return this;
  }

  /**
   * 앨범의 제목을 업데이트합니다.
   * @param title 새 제목
   * @return 업데이트된 앨범 인스턴스
   */
  public Album updateTitle(String title) {
    this.title = title;
    return this;
  }

  /**
   * 앨범의 설명을 업데이트합니다.
   * @param description 새 설명
   * @return 업데이트된 앨범 인스턴스
   */
  public Album updateDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * 앨범의 테마를 업데이트합니다.
   * @param theme 새 테마
   * @return 업데이트된 앨범 인스턴스
   */
  public Album updateTheme(AlbumTheme theme) {
    this.theme = theme;
    return this;
  }

  /**
   * 앨범의 썸네일 URL을 업데이트합니다.
   * @param thumbnailUrl 새 썸네일 URL
   * @return 업데이트된 앨범 인스턴스
   */
  public Album updateThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
    return this;
  }

  /**
   * 앨범의 공개 범위를 업데이트합니다.
   * @param visibility 새 공개 범위
   * @return 업데이트된 앨범 인스턴스
   */
  public Album updateVisibility(AlbumVisibility visibility) {
    this.visibility = visibility;
    return this;
  }
}