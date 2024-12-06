package com.min.i.memory_BE.mock.data;

import com.min.i.memory_BE.domain.album.enums.AlbumTheme;
import com.min.i.memory_BE.domain.album.enums.QuestionTheme;
import com.min.i.memory_BE.domain.media.enums.MediaType;
import com.min.i.memory_BE.mock.dto.response.AlbumResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MockAlbumData {
  private final MockMediaData mockMediaData;
  private final MockUserData mockUserData;
  
  public List<AlbumResponseDto> getMockAlbums() {
    return IntStream.range(0, 5)
      .mapToObj(i -> AlbumResponseDto.builder()
        .id((long) i)
        .title("추억의 앨범 " + (i + 1))
        .description("즐거웠던 우리의 시간")
        .thumbnailUrl(mockMediaData.getRandomImageUrl())
        .theme(AlbumTheme.SENIOR_CARE)
        .mediaList(getMockMediaList())
        .createdBy(mockUserData.getMockUserSimple())
        .build())
      .collect(Collectors.toList());
  }
  
  private List<AlbumResponseDto.MediaDto> getMockMediaList() {
    return mockMediaData.getRandomImageUrls(4).stream()
      .map(url -> AlbumResponseDto.MediaDto.builder()
        .id(new Random().nextLong())
        .fileUrl(url)
        .fileType(MediaType.IMAGE)
        .questions(getMockQuestions())
        .uploadedBy(mockUserData.getMockUserSimple())
        .build())
      .collect(Collectors.toList());
  }
  
  private List<AlbumResponseDto.QuestionDto> getMockQuestions() {
    return List.of(
      AlbumResponseDto.QuestionDto.builder()
        .id(1L)
        .content("이 날 어떤 일이 있었나요?")
        .theme(QuestionTheme.SENIOR_CARE)
        .answers(getMockAnswers())
        .build()
    );
  }
  
  private List<AlbumResponseDto.AnswerDto> getMockAnswers() {
    return List.of(
      AlbumResponseDto.AnswerDto.builder()
        .id(1L)
        .content("정말 즐거운 하루였어요")
        .voiceText("음성 답변 내용입니다...")
        .finalStory("AI가 정리한 최종 이야기입니다...")
        .answeredBy(mockUserData.getMockUserSimple())
        .createdAt(LocalDateTime.now())
        .build()
    );
  }
}
