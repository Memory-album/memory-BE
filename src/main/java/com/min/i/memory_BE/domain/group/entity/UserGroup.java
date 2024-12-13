package com.min.i.memory_BE.domain.group.entity;

import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.enums.UserGroupRole;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_groups",
  uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "group_id"})
  })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserGroup extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id", nullable = false)
  private Group group;
  
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserGroupRole role;
  
  // 그룹별 프로필 정보만 유지
  @Column(name = "group_nickname")
  private String groupNickname;
  
  @Column(name = "group_profile_img_url")
  private String groupProfileImgUrl;
  
  @Builder
  public UserGroup(User user, Group group, UserGroupRole role,
    String groupNickname, String groupProfileImgUrl) {
    this.user = user;
    this.group = group;
    this.role = role;
    this.groupNickname = groupNickname;
    this.groupProfileImgUrl = groupProfileImgUrl;
  }
  
  public void updateGroupProfile(String groupNickname, String groupProfileImgUrl) {
    this.groupNickname = groupNickname;
    this.groupProfileImgUrl = groupProfileImgUrl;
  }
}