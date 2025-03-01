package com.min.i.memory_BE.domain.media.repository;

import com.min.i.memory_BE.domain.media.entity.Media;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
  
  @Query("SELECT m FROM Media m WHERE m.album.id = :albumId AND m.album.group.id = :groupId")
  Page<Media> findByAlbumIdAndGroupId(Long albumId, Long groupId, Pageable pageable);
  
  @Query("SELECT m FROM Media m WHERE m.fileUrl = :fileUrl AND m.album.group.id = :groupId")
  Optional<Media> findByFileUrlAndGroupId(String fileUrl, Long groupId);
  
  // 앨범 내 미디어 존재 여부 확인
  boolean existsByAlbumIdAndAlbumGroupId(Long albumId, Long groupId);
}

