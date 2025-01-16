package com.min.i.memory_BE.domain.group.repository;
import com.min.i.memory_BE.domain.group.entity.Group;
import com.min.i.memory_BE.domain.group.entity.UserGroup;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.enums.UserGroupRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
  Optional<UserGroup> findByUserAndGroup(User user, Group group);
  
  List<UserGroup> findByGroup(Group group);
  
  List<UserGroup> findByUser(User user);
  
  List<UserGroup> findByGroupAndRole(Group group, UserGroupRole role);
  
  boolean existsByUserAndGroup(User user, Group group);
  
  @Query("SELECT MAX(ug.sortOrder) FROM UserGroup ug WHERE ug.user.id = :userId")
  Integer findMaxSortOrderByUser(Long userId);
}