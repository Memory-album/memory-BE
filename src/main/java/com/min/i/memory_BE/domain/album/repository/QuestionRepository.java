package com.min.i.memory_BE.domain.album.repository;

import com.min.i.memory_BE.domain.album.entity.Question;
import com.min.i.memory_BE.domain.media.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByMedia(Media media);
    List<Question> findByMediaId(Long mediaId);
    
    @Query("SELECT q FROM Question q JOIN FETCH q.media m JOIN FETCH m.uploadedBy WHERE q.id = :questionId")
    Optional<Question> findByIdWithMediaAndUploader(@Param("questionId") Long questionId);
    
    @Query("SELECT q FROM Question q JOIN FETCH q.media m JOIN FETCH m.uploadedBy WHERE q.media.id = :mediaId")
    List<Question> findByMediaIdWithMediaAndUploader(@Param("mediaId") Long mediaId);
} 