package com.min.i.memory_BE.domain.user.entity;

import com.min.i.memory_BE.domain.group.entity.UserGroup;
import com.min.i.memory_BE.domain.user.enums.UserStatus;
import com.min.i.memory_BE.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users") // 예약어 보호
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

  private String profileImgUrl = "";

  @Column(nullable = false)
  private boolean emailVerified = false;

  private String emailVerificationCode;

  // 이메일 인증 코드 유효기간
  private LocalDateTime emailVerificationExpiredAt;

  // 로그인 시도 횟수
  private int loginAttempts = 0;  // 기본값 0

  // 계정 잠금 여부
  private boolean accountLocked = false;

  // 마지막 로그인 시도 시간
  private LocalDateTime lastLoginAttempt;

  // 계정 잠금 해제까지 남은 시간
  private LocalDateTime lockedUntil;

  // 사용자 상태 (활성, 비활성, 삭제됨)
  @Enumerated(EnumType.STRING)  // 열거형을 DB에 문자열로 저장
  @Column(nullable = false)
  private UserStatus status = UserStatus.ACTIVE;  // 기본값을 'ACTIVE'로 설정

  @OneToMany(mappedBy = "user")
  private final List<OAuthAccount> oauthAccounts = new ArrayList<>();

  @OneToMany(mappedBy = "user")
  private final List<UserGroup> userGroups = new ArrayList<>();

  @Builder(toBuilder = true)  // toBuilder 활성화
  public User(Long id, String email, String password, String name, boolean emailVerified, String profileImgUrl, String emailVerificationCode,
    LocalDateTime emailVerificationExpiredAt, int loginAttempts, boolean accountLocked, LocalDateTime lastLoginAttempt, LocalDateTime lockedUntil, UserStatus status) {
    this.id = id;
    this.email = email;
    this.password = password;
    this.name = name;
    this.profileImgUrl = profileImgUrl != null ? profileImgUrl : ""; // null 체크 추가
    this.emailVerified = emailVerified;
    this.emailVerificationCode = emailVerificationCode;
    this.emailVerificationExpiredAt = emailVerificationExpiredAt;
    this.loginAttempts = loginAttempts;
    this.accountLocked = accountLocked;
    this.lastLoginAttempt = lastLoginAttempt;
    this.lockedUntil = lockedUntil;
    this.status = status != null ? status : UserStatus.ACTIVE;  // 기본값을 ACTIVE로 설정
  }
}
