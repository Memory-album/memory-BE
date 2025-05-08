package com.min.i.memory_BE.domain.album.repository;

import com.min.i.memory_BE.domain.album.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 스토리 엔티티에 접근하는 리포지토리 인터페이스
 */
@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    
    /**
     * 미디어 ID로 스토리를 조회합니다.
     * 
     * @param mediaId 미디어 ID
     * @return 스토리 (없는 경우 Optional.empty())
     */
    Optional<Story> findByMediaId(Long mediaId);

    /**
     * 미디어 ID로 스토리 존재 여부를 확인합니다.
     *
     * @param mediaId 미디어 ID
     * @return 존재 여부 (true/false)
     */
    boolean existsByMediaId(Long mediaId);
} 