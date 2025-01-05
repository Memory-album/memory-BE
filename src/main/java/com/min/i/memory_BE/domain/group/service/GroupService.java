package com.min.i.memory_BE.domain.group.service;

import com.min.i.memory_BE.domain.group.dto.request.GroupRequestDto;
import com.min.i.memory_BE.domain.group.dto.response.GroupResponseDto;
import com.min.i.memory_BE.domain.group.entity.Group;
import com.min.i.memory_BE.domain.group.entity.UserGroup;
import com.min.i.memory_BE.domain.group.repository.GroupRepository;
import com.min.i.memory_BE.domain.group.repository.UserGroupRepository;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.enums.UserGroupRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GroupService {
  private final GroupRepository groupRepository;
  private final UserGroupRepository userGroupRepository;
  
  @Transactional
  public GroupResponseDto createGroup(GroupRequestDto request, User user) {
    Group group = Group.builder()
      .name(request.getName())
      .groupDescription(request.getGroupDescription())
      .groupImageUrl(request.getGroupImageUrl())
      .build();
    
    Group savedGroup = groupRepository.save(group);
    
    UserGroup userGroup = UserGroup.builder()
      .user(user)
      .group(savedGroup)
      .role(UserGroupRole.OWNER)
      .build();
    
    userGroupRepository.save(userGroup);
    
    
    return GroupResponseDto.from(savedGroup);
  }
}
