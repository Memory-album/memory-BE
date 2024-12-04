package com.min.i.memory_BE.domain.user.entity;

import com.min.i.memory_BE.domain.user.enums.OAuthProvider;
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
@Table(name = "oauth_accounts",
  uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "provider_user_id"})
  })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuthAccount extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;
  
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OAuthProvider provider;
  
  @Column(name = "provider_user_id", nullable = false)
  private String providerUserId;
  
  private String accessToken;
  
  private String refreshToken;
  
  private LocalDateTime tokenExpiresAt;
  
  @Builder
  public OAuthAccount(User user, OAuthProvider provider, String providerUserId,
    String accessToken, String refreshToken, LocalDateTime tokenExpiresAt) {
    this.user = user;
    this.provider = provider;
    this.providerUserId = providerUserId;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.tokenExpiresAt = tokenExpiresAt;
  }
}
