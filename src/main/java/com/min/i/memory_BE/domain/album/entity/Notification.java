package com.min.i.memory_BE.domain.album.entity;

import com.min.i.memory_BE.domain.album.enums.NotificationType;
import com.min.i.memory_BE.domain.user.entity.User;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;
  
  @Column(nullable = false)
  private String title;
  
  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;
  
  @Enumerated(EnumType.STRING)
  private NotificationType notificationType;
  
  private Long referenceId;
  
  private boolean isRead = false;
  
  @Builder
  public Notification(User user, String title, String content,
    NotificationType notificationType, Long referenceId) {
    this.user = user;
    this.title = title;
    this.content = content;
    this.notificationType = notificationType;
    this.referenceId = referenceId;
  }
  
  public void markAsRead() {
    this.isRead = true;
  }
}