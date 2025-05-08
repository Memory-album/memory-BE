package com.min.i.memory_BE.domain.media.repository;

import com.min.i.memory_BE.domain.media.entity.Keyword;
import com.min.i.memory_BE.domain.media.entity.Media;
import com.min.i.memory_BE.domain.media.entity.MediaKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaKeywordRepository extends JpaRepository<MediaKeyword, Long> {
    Optional<MediaKeyword> findByMediaAndKeyword(Media media, Keyword keyword);
    List<MediaKeyword> findByMediaId(Long mediaId);
} 