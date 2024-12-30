package com.min.i.memory_BE.global.config;

import com.min.i.memory_BE.domain.user.service.JwtTokenProvider;
import com.min.i.memory_BE.domain.user.service.MyUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

  @Autowired
  private MyUserDetailsService myUserDetailsService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  @Bean
  public JWTAuthenticationFilter jwtAuthenticationFilter() {
    return new JWTAuthenticationFilter(jwtTokenProvider);  // JWTAuthenticationFilter를 빈으로 등록
  }

  @Bean
  public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
    return http.getSharedObject(AuthenticationManagerBuilder.class)
            .userDetailsService(myUserDetailsService)
            .passwordEncoder(passwordEncoder)
            .and()
            .build();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .csrf().disable()  // CSRF 보호 비활성화
            .authorizeRequests()
              .requestMatchers("/h2-console/**" ,"/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/api/v1/mock/**", "/user/send-verification-code", "/user/verify-email", "/user/complete-register")  // 로그인 및 회원가입 URL은 인증 없이 접근 허용
              .permitAll()
              .requestMatchers("/auth/login").permitAll()  // 로그인 URL을 인증 없이 접근 허용
              .requestMatchers("/user/home").authenticated()// 홈 페이지는 로그인한 사용자만 접근 가능
              .requestMatchers("/user/my-page").authenticated()  // 마이 페이지도 로그인한 사용자만 접근 가능
              .anyRequest().authenticated()  // 그 외 모든 요청은 인증 필요
            .and()
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)  // JWT 필터 추가
            .logout()
              .logoutUrl("/logout")
              .logoutSuccessUrl("/login?logout")
              .invalidateHttpSession(true)  // 세션 무효화
              .deleteCookies("JSESSIONID")  // 쿠키 삭제
              .permitAll()
            .and()
            // X-Frame-Options를 허용하도록 설정
            .headers()
            .frameOptions().sameOrigin();  // H2 콘솔이 iframe 안에서 실행되도록 설정

    return http.build();
  }

  @Autowired
  public void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(myUserDetailsService).passwordEncoder(passwordEncoder);  // PasswordEncoder를 사용
  }

  @Bean
  public AuthenticationFailureHandler authenticationFailureHandler() {
    return (request, response, exception) -> {
      logger.error("로그인 실패: {}", exception.getMessage());  // 로그인 실패 이유를 로그에 출력
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("로그인에 실패했습니다. 다시 시도해 주세요.");
    };
  }

}
