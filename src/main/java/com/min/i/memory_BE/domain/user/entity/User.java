package com.min.i.memory_BE.domain.user.entity;

import com.min.i.memory_BE.domain.group.entity.UserGroup;
import com.min.i.memory_BE.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(unique = true, nullable = false)
  private String email;
  
  private String password;
  
  @Column(nullable = false)
  private String name;
  
  private String profileImgUrl;

  @Column(nullable = false)
  private boolean emailVerified = false;

  private String emailVerificationCode;
  
  private LocalDateTime emailVerificationExpiredAt;// 유효기간
  
  @OneToMany(mappedBy = "user")
  private final List<OAuthAccount> oauthAccounts = new ArrayList<>();
  
  @OneToMany(mappedBy = "user")
  private final List<UserGroup> userGroups = new ArrayList<>();
  
  @Builder
  public User(String email, String password, String name, boolean emailVerified, String profileImageUrl, String emailVerificationCode,
    LocalDateTime emailVerificationExpiredAt) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.profileImgUrl = profileImageUrl;
    this.emailVerified = emailVerified;
    this.emailVerificationCode = emailVerificationCode;
    this.emailVerificationExpiredAt = emailVerificationExpiredAt;
  }
}
