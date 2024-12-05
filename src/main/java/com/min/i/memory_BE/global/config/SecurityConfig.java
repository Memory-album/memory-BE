package com.min.i.memory_BE.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(AbstractHttpConfigurer::disable)  // CSRF 보호 비활성화
      .headers(headers ->
        headers.frameOptions(
          FrameOptionsConfig::disable
        )
      )
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(
          "/h2-console/**",
          "/swagger-ui/**",
          "/api-docs/**"
        ).permitAll()
        .anyRequest().authenticated()
      );

    return http.build();
  }
}