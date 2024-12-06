package com.min.i.memory_BE.mock.dto.response;

import com.min.i.memory_BE.domain.album.enums.AlbumTheme;
import com.min.i.memory_BE.domain.album.enums.QuestionTheme;
import com.min.i.memory_BE.domain.media.enums.MediaType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class AlbumResponseDto {
  private Long id;
  private String title;
  private String description;
  private String thumbnailUrl;
  private AlbumTheme theme;
  private List<MediaDto> mediaList;
  private UserSimpleDto createdBy;
  
  @Getter @Builder
  public static class MediaDto {
    private Long id;
    private String fileUrl;
    private MediaType fileType;
    private List<QuestionDto> questions;
    private UserSimpleDto uploadedBy;
  }
  
  @Getter @Builder
  public static class UserSimpleDto {
    private Long id;
    private String name;
    private String profileImgUrl;
  }
  
  @Getter @Builder
  public static class QuestionDto {
    private Long id;
    private String content;
    private QuestionTheme theme;
    private List<AnswerDto> answers;
  }
  
  @Getter
  @Builder
  public static class AnswerDto {
    private Long id;
    private String content;
    private String voiceText;
    private String finalStory;
    private UserSimpleDto answeredBy;
    private LocalDateTime createdAt;
  }
}
