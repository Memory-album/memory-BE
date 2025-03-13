package com.min.i.memory_BE.domain.album.entity;

import com.min.i.memory_BE.domain.media.entity.Media;
import com.min.i.memory_BE.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Story extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id")
    private Media media;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder
    public Story(Media media, String content) {
        this.media = media;
        this.content = content;
    }
}