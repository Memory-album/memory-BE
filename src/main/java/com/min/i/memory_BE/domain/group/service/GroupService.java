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
      .groupNickname(user.getName())
      .groupProfileImgUrl(user.getProfileImgUrl())
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
  
  @Transactional
  public GroupResponseDto updateGroupImage(Long groupId, MultipartFile file, String email) {
    // 1. 그룹과 사용자 조회
    Group group = groupRepository.findById(groupId)
      .orElseThrow(() -> new EntityNotFoundException("Group not found"));
    
    User user = userRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("User not found"));
    
    // 2. 권한 체크 (그룹 멤버인지)
    UserGroup userGroup = userGroupRepository.findByUserAndGroup(user, group)
      .orElseThrow(() -> new IllegalArgumentException("Not a member of this group"));
    
    // 3. 이전 이미지 URL 저장 (삭제를 위해)
    String oldImageUrl = group.getGroupImageUrl();
    
    // 4. 새 이미지 업로드
    String newImageUrl = s3Service.uploadGroupImage(file, groupId);
    
    // 5. 그룹 정보 업데이트
    Group updatedGroup = Group.builder()
      .name(group.getName())
      .groupDescription(group.getGroupDescription())
      .groupImageUrl(newImageUrl)
      .inviteCode(group.getInviteCode())
      .inviteCodeExpiryAt(group.getInviteCodeExpiryAt())
      .isInviteCodeActive(group.isInviteCodeActive())
      .build();
    
    groupRepository.save(updatedGroup);
    
    // 6. 이전 이미지 삭제
    if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
        s3Service.deleteImage(oldImageUrl);
    }
    
    return GroupResponseDto.from(updatedGroup, userGroup);
  }
}