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
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(nullable = false)
  private String name;
  
  @Column(unique = true)
  private String inviteCode;
  
  private String groupImageUrl;
  
  @OneToMany(mappedBy = "group")
  private List<UserGroup> userGroups = new ArrayList<>();
  
  @OneToMany(mappedBy = "group")
  private List<Album> albums = new ArrayList<>();
  
  @Builder
  public Group(String name, String inviteCode, String groupImageUrl) {
    this.name = name;
    this.inviteCode = inviteCode;
    this.groupImageUrl = groupImageUrl;
  }
}

