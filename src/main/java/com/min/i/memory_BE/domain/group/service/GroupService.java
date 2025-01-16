package com.min.i.memory_BE.domain.group.service;
import com.min.i.memory_BE.domain.group.dto.request.GroupRequestDto;
import com.min.i.memory_BE.domain.group.dto.response.GroupResponseDto;
import com.min.i.memory_BE.domain.group.entity.Group;
import com.min.i.memory_BE.domain.group.entity.UserGroup;
import com.min.i.memory_BE.domain.group.repository.GroupRepository;
import com.min.i.memory_BE.domain.group.repository.UserGroupRepository;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.enums.UserGroupRole;
import com.min.i.memory_BE.global.error.exception.EntityNotFoundException;
import com.min.i.memory_BE.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GroupService {
  private final GroupRepository groupRepository;
  private final UserGroupRepository userGroupRepository;
  private final UserRepository userRepository;
  
  @Transactional
  public GroupResponseDto createGroup(GroupRequestDto.Create request, String email) {
    User user = userRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("User not found"));
    
    // 그룹 생성
    Group group = Group.builder()
      .name(request.getName())
      .groupDescription(request.getGroupDescription())
      .groupImageUrl(request.getGroupImageUrl())
      .inviteCode(generateInviteCode())
      .inviteCodeExpiryAt(LocalDateTime.now().plusDays(7))
      .isInviteCodeActive(true)
      .build();
    
    groupRepository.save(group);
    
    Integer maxSortOrder = userGroupRepository.findMaxSortOrderByUser(user.getId());
    int nextSortOrder = (maxSortOrder != null) ? maxSortOrder + 1 : 1;
    
    UserGroup userGroup = UserGroup.builder()
      .user(user)
      .group(group)
      .role(UserGroupRole.OWNER)
      .groupNickname(request.getName())
      .groupProfileImgUrl(request.getGroupImageUrl())
      .notificationEnabled(true)
      .sortOrder(nextSortOrder)
      .lastVisitAt(LocalDateTime.now())
      .build();
    
    UserGroup savedUserGroup = userGroupRepository.save(userGroup);
    
    return GroupResponseDto.from(group, savedUserGroup);
  }
  
  private String generateInviteCode() {
    return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
  }
}