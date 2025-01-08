package com.min.i.memory_BE.domain.group.repository;

import com.min.i.memory_BE.domain.group.entity.Group;
import com.min.i.memory_BE.domain.user.enums.UserGroupRole;
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
  
  // 사용자의 특정 역할을 가진 그룹 목록 조회
  @Query("SELECT g FROM Group g JOIN g.userGroups ug " +
    "WHERE ug.user.id = :userId AND ug.role = :role")
  List<Group> findAllByUserIdAndRole(Long userId, UserGroupRole role);
  
  // 그룹 내 특정 역할을 가진 멤버 수 조회
  @Query("SELECT COUNT(ug) FROM UserGroup ug " +
    "WHERE ug.group.id = :groupId AND ug.role = :role")
  long countByGroupIdAndRole(Long groupId, UserGroupRole role);
  
  // 활성화된 초대 코드를 가진 그룹 조회
  @Query("SELECT g FROM Group g WHERE g.inviteCode = :inviteCode " +
    "AND g.inviteCodeExpiryAt > :now AND g.isInviteCodeActive = true")
  Optional<Group> findByActiveInviteCode(String inviteCode, LocalDateTime now);
}

