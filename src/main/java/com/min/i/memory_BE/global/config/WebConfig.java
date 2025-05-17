package com.min.i.memory_BE.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
      .allowedOrigins("http://localhost:3000","https://min-21h87evi1-yeonguks-projects.vercel.app","https://min-i.vercel.app")  // React 개발 서버
      .allowedMethods("*")
      .allowedHeaders("*");

    // 추가: /user/** 경로에도 CORS 허용
    registry.addMapping("/user/**")
            .allowedOrigins("http://localhost:3000","https://min-21h87evi1-yeonguks-projects.vercel.app","https://min-i.vercel.app")
            .allowedMethods("*")
            .allowCredentials(true) // 쿠키 포함 요청 허용
            .allowedHeaders("*");

    registry.addMapping("/register/**")
            .allowedOrigins("http://localhost:3000","https://min-21h87evi1-yeonguks-projects.vercel.app","https://min-i.vercel.app")
            .allowedMethods("*")
            .allowCredentials(true) // 쿠키 포함 요청 허용
            .allowedHeaders("*");

    registry.addMapping("/auth/**")
            .allowedOrigins("http://localhost:3000","https://min-21h87evi1-yeonguks-projects.vercel.app","https://min-i.vercel.app")
            .allowedMethods("*")
            .allowCredentials(true) // 쿠키 포함 요청 허용
            .allowedHeaders("*");

    // 추가: /user/** 경로에도 CORS 허용
    registry.addMapping("/oauth/**")
            .allowedOrigins("http://localhost:3000","https://min-21h87evi1-yeonguks-projects.vercel.app","https://min-i.vercel.app")
            .allowedMethods("*")
            .allowedHeaders("*");
  }




}