package com.min.i.memory_BE.domain.user.entity;

import com.min.i.memory_BE.domain.group.entity.UserGroup;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "users")
@Entity
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
  
  private LocalDate dateOfBirth;
  
  @Enumerated(EnumType.STRING)
  private final UserStatus status = UserStatus.ACTIVE;
  
  @OneToMany(mappedBy = "user")
  private final List<OAuthAccount> oauthAccounts = new ArrayList<>();
  
  @OneToMany(mappedBy = "user")
  private final List<UserGroup> userGroups = new ArrayList<>();
  
  
  @Builder
  public User(String email, String password, String name,
    String profileImageUrl, LocalDate dateOfBirth) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.profileImgUrl = profileImageUrl;
    this.dateOfBirth = dateOfBirth;
  }
}
