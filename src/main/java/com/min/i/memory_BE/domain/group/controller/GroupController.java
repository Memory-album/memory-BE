package com.min.i.memory_BE.domain.group.controller;

import com.min.i.memory_BE.domain.group.dto.response.GroupListResponseDto;
import com.min.i.memory_BE.domain.group.dto.response.GroupResponseDto;
import com.min.i.memory_BE.domain.group.service.GroupService;
import com.min.i.memory_BE.domain.user.security.CustomUserDetails;
import com.min.i.memory_BE.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

  @Operation(summary = "그룹 이미지 업로드/수정")
  @PutMapping(value = "/{groupId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<GroupResponseDto>> updateGroupImage(
    @Parameter(description = "그룹 ID") @PathVariable Long groupId,
    @Parameter(description = "그룹 이미지 파일") @RequestPart("file") MultipartFile file,
    @AuthenticationPrincipal String email
  ) {
    GroupResponseDto response = groupService.updateGroupImage(groupId, file, email);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
  
  @GetMapping
  @Operation(summary = "사용자의 모든 그룹 조회")
  public ResponseEntity<ApiResponse<List<GroupResponseDto>>> getGroups(
    @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    List<GroupResponseDto> response = groupService.getGroups(userDetails.getEmail());
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
}

