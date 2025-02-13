package com.min.i.memory_BE.domain.group.controller;

import com.min.i.memory_BE.domain.group.dto.request.GroupJoinRequestDto;
import com.min.i.memory_BE.domain.group.dto.request.GroupRequestDto;
import com.min.i.memory_BE.domain.group.dto.response.GroupJoinResponseDto;
import com.min.i.memory_BE.domain.group.dto.response.GroupListResponseDto;
import com.min.i.memory_BE.domain.group.dto.response.GroupMemberResponseDto;
import com.min.i.memory_BE.domain.group.dto.response.GroupResponseDto;
import com.min.i.memory_BE.domain.group.dto.response.GroupDetailResponseDto;
import com.min.i.memory_BE.domain.group.entity.UserGroup;
import com.min.i.memory_BE.domain.group.service.GroupService;
import com.min.i.memory_BE.domain.user.security.CustomUserDetails;
import com.min.i.memory_BE.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Tag(name = "Group API", description = "그룹 관리 API")
public class GroupController {
  private final GroupService groupService;
  
  @Operation(summary = "그룹 생성")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<GroupResponseDto>> createGroup(
    @RequestPart("name") String name,
    @RequestPart("groupDescription") String groupDescription,
    @RequestPart(value = "groupImageUrl", required = false) MultipartFile groupImageUrl,
    @AuthenticationPrincipal CustomUserDetails userDetails) {
    
    GroupResponseDto response = groupService.createGroup(
      name,
      groupDescription,
      groupImageUrl,
      userDetails.getEmail()  // CustomUserDetails에서 이메일 가져오기
    );
    return ResponseEntity.ok(ApiResponse.success(response));
  }
  
  @Operation(summary = "그룹 정보 수정")
  @PutMapping(value = "/{groupId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<GroupResponseDto>> updateGroup(
    @Parameter(description = "그룹 ID") @PathVariable Long groupId,
    @Parameter(description = "그룹 이름") @RequestPart("name") String name,
    @Parameter(description = "그룹 설명") @RequestPart(value = "groupDescription", required = false) String groupDescription,
    @Parameter(description = "그룹 이미지") @RequestPart(value = "groupImage", required = false) MultipartFile groupImage,
    @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    GroupRequestDto.Update request = GroupRequestDto.Update.builder()
      .name(name)
      .groupDescription(groupDescription)
      .build();
    
    GroupResponseDto response = groupService.updateGroup(groupId, request, groupImage, userDetails.getEmail());
    return ResponseEntity.ok(ApiResponse.success(response));
  }
  
  @Operation(summary = "내가 속한 그룹 목록 조회")
  @GetMapping("/my-groups")
  public ResponseEntity<ApiResponse<List<GroupListResponseDto>>> getMyGroups(
    @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    List<GroupListResponseDto> response = groupService.getMyGroups(userDetails.getEmail());
    return ResponseEntity.ok(ApiResponse.success(response));
  }
  
  @Operation(summary = "그룹 참여하기")
  @PostMapping("/join")
  public ResponseEntity<ApiResponse<GroupJoinResponseDto>> joinGroup(
    @Valid @RequestBody GroupJoinRequestDto request,
    @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    GroupJoinResponseDto response = groupService.joinGroup(request, userDetails.getEmail());
    return ResponseEntity.ok(ApiResponse.success(response));
  }
  
  @Operation(summary = "그룹 나가기")
  @DeleteMapping("/{groupId}/leave")
  public ResponseEntity<ApiResponse<Void>> leaveGroup(
    @Parameter(description = "그룹 ID") @PathVariable Long groupId,
    @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    groupService.leaveGroup(groupId, userDetails.getEmail());
    return ResponseEntity.ok(ApiResponse.success(null));
  }
  
  @Operation(summary = "그룹 멤버 목록 조회")
  @GetMapping("/{groupId}/members")
  public ResponseEntity<ApiResponse<List<GroupMemberResponseDto>>> getGroupMembers(
    @PathVariable Long groupId,
    @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    List<UserGroup> members = groupService.getGroupMembers(groupId, userDetails.getEmail());
    List<GroupMemberResponseDto> response = members.stream()
      .map(GroupMemberResponseDto::from)
      .collect(Collectors.toList());
    
    return ResponseEntity.ok(ApiResponse.success(response));
  }
  
  @DeleteMapping("/{groupId}/members/{memberId}")
  @Operation(summary = "그룹 멤버 삭제")
  public ResponseEntity<ApiResponse<Void>> removeMember(
    @Parameter(description = "그룹 ID") @PathVariable Long groupId,
    @Parameter(description = "삭제할 멤버 ID") @PathVariable Long memberId,
    @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    groupService.removeMember(groupId, memberId, userDetails.getEmail());
    return ResponseEntity.ok(ApiResponse.success(null));
  }
  
  @Operation(summary = "오너 위임 (소유권 이전): 현재 오너가 다른 구성원을 새 오너로 지정합니다.")
  @PostMapping("/{groupId}/transfer-ownership/{newOwnerId}")
  public ResponseEntity<ApiResponse<GroupResponseDto>> transferOwnership(
    @Parameter(description = "그룹 ID") @PathVariable Long groupId,
    @Parameter(description = "새 오너의 사용자 ID") @PathVariable Long newOwnerId,
    @AuthenticationPrincipal CustomUserDetails userDetails) {
    GroupResponseDto response = groupService.transferOwnership(groupId, newOwnerId, userDetails.getEmail());
    return ResponseEntity.ok(ApiResponse.success(response));
  }
  
  @Operation(summary = "시니어 지정: 오너가 구성원을 시니어로 지정합니다.")
  @PutMapping("/{groupId}/appoint-senior/{memberId}")
  public ResponseEntity<ApiResponse<GroupResponseDto>> appointSenior(
    @Parameter(description = "그룹 ID") @PathVariable Long groupId,
    @Parameter(description = "시니어로 지정할 구성원 사용자 ID") @PathVariable Long memberId,
    @AuthenticationPrincipal CustomUserDetails userDetails) {
    GroupResponseDto response = groupService.appointSenior(groupId, memberId, userDetails.getEmail());
    return ResponseEntity.ok(ApiResponse.success(response));
  }
  
  @Operation(summary = "그룹 상세 조회")
  @GetMapping("/{groupId}")
  public ResponseEntity<ApiResponse<GroupDetailResponseDto>> getGroupDetail(
          @PathVariable Long groupId,
          @AuthenticationPrincipal CustomUserDetails userDetails) {
      GroupDetailResponseDto response = groupService.getGroupDetail(groupId, userDetails.getEmail());
      return ResponseEntity.ok(ApiResponse.success(response));
  }
}

