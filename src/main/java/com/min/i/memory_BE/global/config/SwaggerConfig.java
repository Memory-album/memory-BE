package com.min.i.memory_BE.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
  
  @Bean
  public OpenAPI openAPI() {
    Info info = new Info()
      .title("Memory API Documentation")
      .version("v1.0")
      .description("Memory 서비스의 API 문서");
    
    return new OpenAPI()
      .info(info);
  }
}