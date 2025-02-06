package com.min.i.memory_BE.domain.group.service;

import com.min.i.memory_BE.domain.group.dto.response.GroupResponseDto;
import com.min.i.memory_BE.domain.group.entity.Group;
import com.min.i.memory_BE.domain.group.entity.UserGroup;
import com.min.i.memory_BE.domain.group.repository.GroupRepository;
import com.min.i.memory_BE.domain.group.repository.UserGroupRepository;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.enums.UserGroupRole;
import com.min.i.memory_BE.global.error.exception.EntityNotFoundException;
import com.min.i.memory_BE.domain.user.repository.UserRepository;
import com.min.i.memory_BE.global.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GroupService {
  private final GroupRepository groupRepository;
  private final UserGroupRepository userGroupRepository;
  private final UserRepository userRepository;
  private final S3Service s3Service;
  
  @Transactional
  public GroupResponseDto createGroup(String name, String groupDescription, MultipartFile groupImageUrl, String email) {
    // 1. 사용자 조회
    User user = userRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("User not found"));
    
    // 2. 먼저 Group 엔티티를 생성하고 저장
    Group group = Group.builder()
      .name(name)
      .groupDescription(groupDescription)
      .inviteCode(generateInviteCode())
      .inviteCodeExpiryAt(LocalDateTime.now().plusDays(7))
      .isInviteCodeActive(true)
      .build();
    
    Group savedGroup = groupRepository.save(group);
    
    // 3. 이미지가 제공된 경우 S3에 업로드하고 URL 업데이트
    String imageUrl = null;
    if (groupImageUrl != null && !groupImageUrl.isEmpty()) {
      try {
        imageUrl = s3Service.uploadGroupImage(groupImageUrl, savedGroup.getId());
        savedGroup.setGroupImageUrl(imageUrl);
        savedGroup = groupRepository.save(savedGroup);
      } catch (Exception e) {
        throw new RuntimeException("Failed to upload group image", e);
      }
    }
    
    // 4. UserGroup 생성 및 저장
    Integer maxSortOrder = userGroupRepository.findMaxSortOrderByUser(user.getId());
    int nextSortOrder = (maxSortOrder != null) ? maxSortOrder + 1 : 1;
    
    UserGroup userGroup = UserGroup.builder()
      .user(user)
      .group(savedGroup)
      .role(UserGroupRole.OWNER)
      .groupNickname(user.getName())
      .groupProfileImgUrl(user.getProfileImgUrl())
      .notificationEnabled(true)
      .sortOrder(nextSortOrder)
      .lastVisitAt(LocalDateTime.now())
      .build();
    
    UserGroup savedUserGroup = userGroupRepository.save(userGroup);
    
    return GroupResponseDto.from(savedGroup, savedUserGroup);
  }
  
  private String generateInviteCode() {
    return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
  }
  
  @Transactional
  public GroupResponseDto updateGroupImage(Long groupId, MultipartFile file, String email) {
    // 그룹과 사용자 조회
    Group group = groupRepository.findById(groupId)
      .orElseThrow(() -> new EntityNotFoundException("Group not found"));
    
    User user = userRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("User not found"));
    
    // 권한 체크 (그룹 멤버인지)
    UserGroup userGroup = userGroupRepository.findByUserAndGroup(user, group)
      .orElseThrow(() -> new IllegalArgumentException("Not a member of this group"));
    
    // 이전 이미지 URL 저장 (삭제를 위해)
    String oldImageUrl = group.getGroupImageUrl();
    
    // 새 이미지 업로드
    String newImageUrl = s3Service.uploadGroupImage(file, group.getId());
    
    // 그룹 정보 업데이트
    group.setGroupImageUrl(newImageUrl);
    groupRepository.save(group);
    
    // 이전 이미지 삭제
    if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
        s3Service.deleteImage(oldImageUrl);
    }
    
    return GroupResponseDto.from(group, userGroup);
  }
  
  public GroupResponseDto getGroup(Long groupId, String email) {
    User user = userRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    
    Group group = groupRepository.findById(groupId)
      .orElseThrow(() -> new EntityNotFoundException("그룹을 찾을 수 없습니다."));
    
    UserGroup userGroup = userGroupRepository.findByUserAndGroup(user, group)
      .orElseThrow(() -> new EntityNotFoundException("해당 그룹에 속해있지 않습니다."));
    
    return GroupResponseDto.from(group, userGroup);
  }
}