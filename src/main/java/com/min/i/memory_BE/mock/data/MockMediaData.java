package com.min.i.memory_BE.mock.data;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MockMediaData {
  private final List<String> IMAGE_PATHS = List.of(
    "/images/1.png",
    "/images/2.png",
    "/images/3.png",
    "/images/4.png",
    "/images/5.png",
    "/images/6.png"
  );
  
  public String getRandomImageUrl() {
    return IMAGE_PATHS.get(new Random().nextInt(IMAGE_PATHS.size()));
  }
  
  public List<String> getRandomImageUrls(int count) {
    return IMAGE_PATHS.stream()
      .limit(Math.min(count, IMAGE_PATHS.size()))
      .collect(Collectors.toList());
  }
}

