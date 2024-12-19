package com.min.i.memory_BE.domain.user.entity;

import com.min.i.memory_BE.domain.group.entity.UserGroup;
import com.min.i.memory_BE.domain.user.enums.UserMailStatus;
import com.min.i.memory_BE.domain.user.enums.UserStatus;
import com.min.i.memory_BE.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserMailStatus mailStatus = UserMailStatus.UNVERIFIED;

  private String emailVerificationCode;
  
  private LocalDateTime emailVerificationExpiredAt;// 유효기간
  
  @Enumerated(EnumType.STRING)
  private final UserStatus status = UserStatus.ACTIVE;
  
  @OneToMany(mappedBy = "user")
  private final List<OAuthAccount> oauthAccounts = new ArrayList<>();
  
  @OneToMany(mappedBy = "user")
  private final List<UserGroup> userGroups = new ArrayList<>();
  
  @Builder
  public User(String email, String password, String name, String profileImageUrl,
    UserMailStatus mailStatus, String emailVerificationCode,
    LocalDateTime emailVerificationExpiredAt) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.profileImgUrl = profileImageUrl;
    this.mailStatus = mailStatus;
    this.emailVerificationCode = emailVerificationCode;
    this.emailVerificationExpiredAt = emailVerificationExpiredAt;
  }

  // 이메일 인증 완료 후 emailVerified, emailVerificationCode, emailVerificationExpiredAt을 설정하는 메서드 추가
  public void completeEmailVerification() {
    this.mailStatus = mailStatus.VERIFIED;
    this.emailVerificationCode = null;
    this.emailVerificationExpiredAt = null;
  }

  // User 엔티티
  @Builder
  public static User createTemporaryUser(String email, String verificationCode, LocalDateTime expirationTime) {
    return User.builder()
            .email(email)
            .mailStatus(UserMailStatus.UNVERIFIED)  // 이메일 인증되지 않은 상태
            .emailVerificationCode(verificationCode)  // 인증 코드 설정
            .emailVerificationExpiredAt(expirationTime)  // 유효 기간 설정
            .password(null)  // 비밀번호는 아직 없을 수 있음
            .profileImageUrl(null)  // 프로필 이미지도 기본값 null
            .name("ex")
            .build();
  }


  // 최종 가입 완료 처리
  public void completeRegistration(String password, String name, String profileImgUrl) {
    this.mailStatus = UserMailStatus.REGISTERED;
    this.password = password;
    this.name = name;
    this.profileImgUrl = profileImgUrl;
  }

}
