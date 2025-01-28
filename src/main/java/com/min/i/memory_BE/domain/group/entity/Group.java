package com.min.i.memory_BE.domain.group.entity;

import com.min.i.memory_BE.domain.album.entity.Album;
import com.min.i.memory_BE.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "album_groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(nullable = false)
  private String name;
  
  @Column(columnDefinition = "TEXT")
  private String groupDescription;
  
  private String inviteCode;
  
  private LocalDateTime inviteCodeExpiryAt; //유효기간
  
  @Column(nullable = false)
  private boolean isInviteCodeActive = true; //코드활성화 여부
  
  private String groupImageUrl;
  
  @OneToMany(mappedBy = "group")
  private final List<UserGroup> userGroups = new ArrayList<>();
  
  @OneToMany(mappedBy = "group")
  private final List<Album> albums = new ArrayList<>();
  
  @Builder
  public Group(String name, String groupDescription, String inviteCode,
    String groupImageUrl, LocalDateTime inviteCodeExpiryAt,
    boolean isInviteCodeActive) {
    this.name = name;
    this.groupDescription = groupDescription;
    this.inviteCode = inviteCode;
    this.groupImageUrl = groupImageUrl;
    this.inviteCodeExpiryAt = inviteCodeExpiryAt;
    this.isInviteCodeActive = isInviteCodeActive;
  }

  public void setGroupImageUrl(String groupImageUrl) {
    this.groupImageUrl = groupImageUrl;
  }
}

