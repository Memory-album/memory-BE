package com.min.i.memory_BE.domain.group.repository;

import com.min.i.memory_BE.domain.group.entity.Group;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
  // 사용자가 속한 그룹 목록 조회
  @Query("SELECT g FROM Group g JOIN g.userGroups ug WHERE ug.user.id = :userId")
  List<Group> findAllByUserId(Long userId);
  
  // 사용자의 특정 그룹 조회
  @Query("SELECT g FROM Group g JOIN g.userGroups ug " +
    "WHERE g.id = :groupId AND ug.user.id = :userId")
  Optional<Group> findByIdAndUserId(Long groupId, Long userId);
  
  // 초대 코드로 그룹 조회
  Optional<Group> findByInviteCodeAndInviteCodeExpiryAtAfter(
    String inviteCode,
    LocalDateTime currentTime
  );
}

