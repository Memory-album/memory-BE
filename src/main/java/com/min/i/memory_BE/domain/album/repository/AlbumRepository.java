package com.min.i.memory_BE.domain.album.repository;

import com.min.i.memory_BE.domain.album.entity.Album;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
  // 그룹 ID와 앨범 ID로 앨범 조회
  @Query("SELECT a FROM Album a WHERE a.id = :albumId AND a.group.id = :groupId")
  Optional<Album> findByIdAndGroupId(Long albumId, Long groupId);

  // 그룹의 모든 앨범 조회
  @Query("SELECT a FROM Album a WHERE a.group.id = :groupId")
  List<Album> findAllByGroupId(Long groupId);

  // 그룹의 모든 앨범 페이징 조회
  @Query("SELECT a FROM Album a WHERE a.group.id = :groupId")
  Page<Album> findAllByGroupId(Long groupId, Pageable pageable);

  // 앨범이 특정 그룹에 속하는지 확인
  boolean existsByIdAndGroupId(Long albumId, Long groupId);
}