package com.min.i.memory_BE.domain.album.repository;

import com.min.i.memory_BE.domain.album.entity.Question;
import com.min.i.memory_BE.domain.media.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByMedia(Media media);
    List<Question> findByMediaId(Long mediaId);
} 