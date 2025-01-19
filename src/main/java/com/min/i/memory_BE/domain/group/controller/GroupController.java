package com.min.i.memory_BE.domain.group.controller;

import com.min.i.memory_BE.domain.group.dto.request.GroupRequestDto;
import com.min.i.memory_BE.domain.group.dto.response.GroupResponseDto;
import com.min.i.memory_BE.domain.group.service.GroupService;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Tag(name = "Group API", description = "그룹 관리 API")
public class GroupController {
  
  private final GroupService groupService;
  
  @Operation(summary = "그룹 생성")
  @PostMapping
  public ResponseEntity<ApiResponse<GroupResponseDto>> createGroup(
    @Valid @RequestBody GroupRequestDto.Create request,
    @AuthenticationPrincipal String email) {
    
    GroupResponseDto response = groupService.createGroup(request, email);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
  
  @Operation(summary = "그룹 이미지 업로드/수정")
  
  @PutMapping(value = "/{groupId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<GroupResponseDto>> updateGroupImage(
    @Parameter(description = "그룹 ID") @PathVariable Long groupId,
    @Parameter(description = "그룹 이미지 파일") @RequestParam("file") MultipartFile file,
    @AuthenticationPrincipal String email
  ) {
    GroupResponseDto response = groupService.updateGroupImage(groupId, file, email);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}

