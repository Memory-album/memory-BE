package com.min.i.memory_BE.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
  
  @Bean
  public OpenAPI openAPI() {
    Info info = new Info()
      .title("Min:i API Documentation")
      .version("v1.0")
      .description("Min:i API 문서");
    
    // Security Scheme 설정 추가
    SecurityScheme securityScheme = new SecurityScheme()
      .type(SecurityScheme.Type.HTTP) // HTTP 방식 인증
      .scheme("bearer") // Bearer Token 사용
      .bearerFormat("JWT"); // JWT 형식 지정
    
    // Security Requirement 추가
    SecurityRequirement securityRequirement = new SecurityRequirement()
      .addList("BearerAuth");
    
    return new OpenAPI()
      .info(info)
      .addSecurityItem(securityRequirement) // Security Requirement 추가
      .components(new io.swagger.v3.oas.models.Components()
        .addSecuritySchemes("BearerAuth", securityScheme)); // Security Scheme 등록
  }
}