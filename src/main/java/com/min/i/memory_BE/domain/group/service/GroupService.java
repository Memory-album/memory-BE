package com.min.i.memory_BE.domain.group.service;

import com.min.i.memory_BE.domain.group.dto.request.GroupJoinRequestDto;
import com.min.i.memory_BE.domain.group.dto.request.GroupRequestDto;
import com.min.i.memory_BE.domain.group.dto.response.GroupJoinResponseDto;
import com.min.i.memory_BE.domain.group.dto.response.GroupListResponseDto;
import com.min.i.memory_BE.domain.group.dto.response.GroupResponseDto;
import com.min.i.memory_BE.domain.group.dto.response.GroupDetailResponseDto;
import com.min.i.memory_BE.domain.group.entity.Group;
import com.min.i.memory_BE.domain.group.entity.UserGroup;
import com.min.i.memory_BE.domain.group.repository.GroupRepository;
import com.min.i.memory_BE.domain.group.repository.UserGroupRepository;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.enums.UserGroupRole;
import com.min.i.memory_BE.domain.user.repository.UserRepository;
import com.min.i.memory_BE.global.error.exception.EntityNotFoundException;
import com.min.i.memory_BE.global.error.exception.GroupException;
import com.min.i.memory_BE.global.service.S3Service;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
  public GroupResponseDto createGroup(String name, String groupDescription,
    MultipartFile groupImageUrl, String email) {
    User user = userRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("User not found"));
    
    Group group = Group.builder()
      .name(name)
      .groupDescription(groupDescription)
      .inviteCode(generateInviteCode())
      .inviteCodeExpiryAt(LocalDateTime.now().plusDays(7))
      .isInviteCodeActive(true)
      .build();
    
    Group savedGroup = groupRepository.save(group);
    
    String imageUrl = null;
    if (groupImageUrl != null && !groupImageUrl.isEmpty()) {
      try {
        imageUrl = s3Service.uploadGroupImage(groupImageUrl, savedGroup.getId());
        savedGroup.update(savedGroup.getName(), savedGroup.getGroupDescription(), imageUrl);
        savedGroup = groupRepository.save(savedGroup);
      } catch (Exception e) {
        throw new RuntimeException("Failed to upload group image", e);
      }
    }
    
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
  public GroupResponseDto updateGroup(Long groupId, GroupRequestDto.Update request,
    MultipartFile groupImage, String email) {
    
    User user = userRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    
    Group group = groupRepository.findById(groupId)
      .orElseThrow(() -> new EntityNotFoundException("그룹을 찾을 수 없습니다."));
    
    UserGroup userGroup = userGroupRepository.findByUserAndGroup(user, group)
      .orElseThrow(() -> new IllegalArgumentException("그룹에 속하지 않은 사용자입니다."));
    
    if (userGroup.getRole() != UserGroupRole.OWNER) {
      throw new IllegalArgumentException("그룹 정보 수정 권한이 없습니다.");
    }
    
    String imageUrl = group.getGroupImageUrl();
    if (groupImage != null && !groupImage.isEmpty()) {
      imageUrl = s3Service.uploadGroupImage(groupImage, groupId);
    }
    
    group.update(request.getName(), request.getGroupDescription(), imageUrl);
    
    return GroupResponseDto.from(group, userGroup);
  }
  
  @Transactional(readOnly = true)
  public List<GroupListResponseDto> getMyGroups(String email) {
    User user = userRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    
    List<UserGroup> userGroups = userGroupRepository.findByUser(user);
    
    return userGroups.stream()
      .map(userGroup -> GroupListResponseDto.from(userGroup.getGroup()))
      .collect(Collectors.toList());
  }
  
  @Transactional
  public GroupJoinResponseDto joinGroup(GroupJoinRequestDto request, String email) {
    Group group = groupRepository.findByActiveInviteCode(
      request.getInviteCode(),
      LocalDateTime.now()
    ).orElseThrow(() -> new IllegalArgumentException("초대코드가 틀렸습니당"));
    
    User user = userRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다.??"));
    
    if (userGroupRepository.existsByUserAndGroup(user, group)) {
      throw new IllegalArgumentException("이미 이 그룹에 가입되어 있습니다.");
    }
    
    Integer maxSortOrder = userGroupRepository.findMaxSortOrderByUser(user.getId());
    int nextSortOrder = (maxSortOrder != null) ? maxSortOrder + 1 : 1;
    
    String nickname = request.getGroupNickname() != null ?
      request.getGroupNickname() : user.getName();
    
    UserGroup userGroup = UserGroup.builder()
      .user(user)
      .group(group)
      .role(UserGroupRole.MEMBER)
      .groupNickname(nickname)
      .groupProfileImgUrl(user.getProfileImgUrl())
      .notificationEnabled(true)
      .sortOrder(nextSortOrder)
      .lastVisitAt(LocalDateTime.now())
      .build();
    
    UserGroup savedUserGroup = userGroupRepository.save(userGroup);
    return GroupJoinResponseDto.of(group.getId());
    
  }
  
  @Transactional
  public void leaveGroup(Long groupId, String email) {
    User user = userRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    
    Group group = groupRepository.findById(groupId)
      .orElseThrow(GroupException.GroupNotFoundException::new);
    
    UserGroup userGroup = userGroupRepository.findByUserAndGroup(user, group)
      .orElseThrow(GroupException.NotGroupMemberException::new);
    
    if (userGroup.getRole() == UserGroupRole.OWNER) {
      throw new GroupException.OwnerCannotLeaveException();
    }
    
    userGroupRepository.delete(userGroup);
    
    long remainingMembers = userGroupRepository.countByGroup(group);
    if (remainingMembers == 0) {
      groupRepository.delete(group);
    }
  }
  
  @Transactional(readOnly = true)
  public List<UserGroup> getGroupMembers(Long groupId, String email) {
    User user = userRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("유저가 아닙니다.."));
    
    Group group = groupRepository.findById(groupId)
      .orElseThrow(GroupException.GroupNotFoundException::new);
    
    if (!userGroupRepository.existsByUserAndGroup(user, group)) {
      throw new GroupException.NotGroupMemberException();
    }
    
    return userGroupRepository.findByGroupWithUserAndGroup(group);
  }
  
  @Transactional
  public void removeMember(Long groupId, Long memberId, String email) {
    User requester = userRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("유저가 아니에요.."));
    
    Group group = groupRepository.findById(groupId)
      .orElseThrow(GroupException.GroupNotFoundException::new);
    
    UserGroup targetMembership = userGroupRepository.findByUserIdAndGroupId(memberId, groupId)
      .orElseThrow(GroupException.GroupMemberNotFoundException::new);
    
    UserGroup requesterMembership = userGroupRepository.findByUserAndGroup(requester, group)
      .orElseThrow(GroupException.NotGroupMemberException::new);
    
    if (requesterMembership.getRole() != UserGroupRole.OWNER) {
      throw new GroupException.NotOwnerException();
    }
    
    if (targetMembership.getRole() == UserGroupRole.OWNER) {
      throw new GroupException.OwnerCannotBeRemovedException();
    }
    
    userGroupRepository.delete(targetMembership);
  }
  
  @Transactional
  public GroupResponseDto transferOwnership(Long groupId, Long newOwnerId, String currentUserEmail) {
    Group group = groupRepository.findById(groupId)
      .orElseThrow(() -> new EntityNotFoundException("그룹을 찾을 수 없습니다."));
    
    User currentUser = userRepository.findByEmail(currentUserEmail)
      .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    
    UserGroup currentUserGroup = userGroupRepository.findByUserAndGroup(currentUser, group)
      .orElseThrow(GroupException.NotGroupMemberException::new);
    if (!currentUserGroup.getRole().equals(UserGroupRole.OWNER)) {
      throw new GroupException.NotOwnerException();
    }
    
    if (newOwnerId.equals(currentUser.getId())) {
      throw new IllegalArgumentException("새 오너는 현재 오너와 달라야 합니다.");
    }
    
    User newOwner = userRepository.findById(newOwnerId)
      .orElseThrow(() -> new EntityNotFoundException("새 오너 사용자를 찾을 수 없습니다."));
    UserGroup newOwnerGroup = userGroupRepository.findByUserAndGroup(newOwner, group)
      .orElseThrow(GroupException.NotGroupMemberException::new);
    
    updateUserGroupRole(currentUserGroup, UserGroupRole.MEMBER);
    updateUserGroupRole(newOwnerGroup, UserGroupRole.OWNER);
    
    return GroupResponseDto.from(group, newOwnerGroup);
  }
  
  @Transactional
  public GroupResponseDto appointSenior(Long groupId, Long memberId, String currentUserEmail) {
    Group group = groupRepository.findById(groupId)
      .orElseThrow(() -> new EntityNotFoundException("그룹을 찾을 수 없습니다."));
    
    User currentUser = userRepository.findByEmail(currentUserEmail)
      .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    
    UserGroup currentUserGroup = userGroupRepository.findByUserAndGroup(currentUser, group)
      .orElseThrow(GroupException.NotGroupMemberException::new);
    if (!currentUserGroup.getRole().equals(UserGroupRole.OWNER)) {
      throw new GroupException.NotOwnerException();
    }
    
    if (memberId.equals(currentUser.getId())) {
      throw new IllegalArgumentException("오너는 본인을 시니어로 지정할 수 없습니다.");
    }
    
    User member = userRepository.findById(memberId)
      .orElseThrow(() -> new EntityNotFoundException("지정할 사용자를 찾을 수 없습니다."));
    UserGroup memberGroup = userGroupRepository.findByUserAndGroup(member, group)
      .orElseThrow(GroupException.NotGroupMemberException::new);
    
    updateUserGroupRole(memberGroup, UserGroupRole.SENIOR);
    
    return GroupResponseDto.from(group, memberGroup);
  }
  
  private void updateUserGroupRole(UserGroup userGroup, UserGroupRole role) {
    try {
      Field field = UserGroup.class.getDeclaredField("role");
      field.setAccessible(true);
      field.set(userGroup, role);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("UserGroup role 업데이트에 실패했습니다.", e);
    }
  }
  
  public GroupDetailResponseDto getGroupDetail(Long groupId, String email) {
    Group group = groupRepository.findById(groupId)
      .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));
    
    UserGroup myUserGroup = group.getUserGroups().stream()
      .filter(ug -> ug.getUser().getEmail().equals(email))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException("해당 그룹에 속해 있지 않습니다."));
    
    UserGroup ownerUserGroup = group.getUserGroups().stream()
      .filter(ug -> ug.getRole() == UserGroupRole.OWNER)
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("그룹 오너를 찾을 수 없습니다."));
    
    return GroupDetailResponseDto.from(group, myUserGroup, ownerUserGroup);
  }
  
  @Transactional
  public void deleteGroup(Long groupId, String email) {
    User owner = userRepository.findByEmail(email)
      .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    
    Group group = groupRepository.findById(groupId)
      .orElseThrow(GroupException.GroupNotFoundException::new);
    
    UserGroup userGroup = userGroupRepository.findByUserAndGroup(owner, group)
      .orElseThrow(GroupException.NotGroupMemberException::new);
    
    if (userGroup.getRole() != UserGroupRole.OWNER) {
      throw new GroupException.NotOwnerException();
    }
    
    if (group.getGroupImageUrl() != null) {
      s3Service.deleteImage(group.getGroupImageUrl());
    }
    
    userGroupRepository.deleteByGroup(group);
    groupRepository.delete(group);
  }
  
}
