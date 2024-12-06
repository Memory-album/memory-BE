package com.min.i.memory_BE.mock.data;

import com.min.i.memory_BE.domain.user.enums.UserGroupRole;
import com.min.i.memory_BE.mock.dto.response.AlbumResponseDto;
import com.min.i.memory_BE.mock.dto.response.UserResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MockUserData {
  private final MockMediaData mockMediaData;
  
  public UserResponseDto getMockUser() {
    return UserResponseDto.builder()
      .id(1L)
      .email("test@example.com")
      .name("김민니")
      .profileImgUrl(mockMediaData.getRandomImageUrl())
      .role(UserGroupRole.OWNER)
      .unreadAnswers(7)
      .groups(getMockGroups())
      .build();
  }
  
  public List<UserResponseDto.GroupSimpleDto> getMockGroups() {
    return List.of(
      UserResponseDto.GroupSimpleDto.builder()
        .id(1L)
        .name("우리 가족")
        .groupImageUrl(mockMediaData.getRandomImageUrl())
        .build(),
      UserResponseDto.GroupSimpleDto.builder()
        .id(2L)
        .name("미니언즈 모임")
        .groupImageUrl(mockMediaData.getRandomImageUrl())
        .build()
    );
  }
  
  public AlbumResponseDto.UserSimpleDto getMockUserSimple() {
    return AlbumResponseDto.UserSimpleDto.builder()
      .id(1L)
      .name("김민니")
      .profileImgUrl(mockMediaData.getRandomImageUrl())
      .build();
  }
}