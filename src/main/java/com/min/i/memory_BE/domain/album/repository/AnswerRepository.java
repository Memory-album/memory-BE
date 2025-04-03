package com.min.i.memory_BE.domain.album.repository;

import com.min.i.memory_BE.domain.album.entity.Answer;
import com.min.i.memory_BE.domain.media.entity.Media;
import com.min.i.memory_BE.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    // 미디어 ID로 답변 찾기
    List<Answer> findByMediaId(Long mediaId);
    
    // 미디어와 사용자로 답변 찾기
    List<Answer> findByMediaAndUser(Media media, User user);
    
    // 미디어 ID와 사용자 ID로 답변 찾기
    @Query("SELECT a FROM Answer a WHERE a.media.id = :mediaId AND a.user.id = :userId")
    List<Answer> findByMediaIdAndUserId(@Param("mediaId") Long mediaId, @Param("userId") Long userId);
    
    // 미디어 ID로 가장 최근 답변 하나 조회
    @Query("SELECT a FROM Answer a WHERE a.media.id = :mediaId ORDER BY a.createdAt DESC")
    Optional<Answer> findLatestByMediaId(@Param("mediaId") Long mediaId);
}