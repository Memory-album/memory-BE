package com.min.i.memory_BE.domain.group.entity;

import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "user_nicknames",
  uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "target_user_id", "group_id"})
  })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNickname extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "target_user_id", nullable = false)
  private User targetUser;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id", nullable = false)
  private Group group;
  
  @Column(nullable = false)
  private String nickname;
  
  @Builder
  public UserNickname(User user, User targetUser, Group group, String nickname) {
    this.user = user;
    this.targetUser = targetUser;
    this.group = group;
    this.nickname = nickname;
  }
  
  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }
}