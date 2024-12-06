package com.min.i.memory_BE.mock.controller;

import com.min.i.memory_BE.mock.data.MockUserData;
import com.min.i.memory_BE.mock.dto.response.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Mock User API")
@RestController
@RequestMapping("/api/v1/mock/users")
@RequiredArgsConstructor
public class MockUserController {
  private final MockUserData mockUserData;
  
  @Operation(summary = "현재 사용자 정보 조회")
  @GetMapping("/me")
  public ResponseEntity<UserResponseDto> getCurrentUser() {
    return ResponseEntity.ok(mockUserData.getMockUser());
  }
}
