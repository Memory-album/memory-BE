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
import java.time.LocalDateTime;
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
  
  @Column(name = "group_nickname")
  private String groupNickname;
  
  @Column(name = "group_profile_img_url")
  private String groupProfileImgUrl;
  
  @Column(name = "last_visit_at")
  private LocalDateTime lastVisitAt;
  
  @Column(name = "notification_enabled", nullable = false)
  private boolean notificationEnabled = true;
  
  @Column(name = "sort_order")
  private Integer sortOrder;
  
  @Builder
  public UserGroup(User user, Group group, UserGroupRole role,
    String groupNickname, String groupProfileImgUrl, boolean notificationEnabled,LocalDateTime lastVisitAt, Integer sortOrder) {
    this.user = user;
    this.group = group;
    this.role = role;
    this.groupNickname = groupNickname;
    this.lastVisitAt = lastVisitAt;
    this.groupProfileImgUrl = groupProfileImgUrl;
    this.notificationEnabled = notificationEnabled;
    this.sortOrder = sortOrder;
  }
}