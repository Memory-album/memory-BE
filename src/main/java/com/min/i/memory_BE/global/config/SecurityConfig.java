package com.min.i.memory_BE.global.config;

import com.min.i.memory_BE.domain.user.service.MyUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

  @Autowired
  private MyUserDetailsService myUserDetailsService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Bean
  public AuthenticationSuccessHandler authenticationSuccessHandler() {
    return (request, response, authentication) -> {
      logger.info("Login successful, redirecting to /user/home");
      response.sendRedirect("/user/home");  // 로그인 후 홈으로 리디렉션
    };
  }

  @Bean
  public AuthenticationFailureHandler authenticationFailureHandler() {
    return (request, response, exception) -> {
      logger.error("Login failed: {}", exception.getMessage());  // 로그인 실패 이유를 로그에 출력
      response.sendRedirect("/login?error=true");  // 로그인 실패 후 처리
    };
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .csrf(AbstractHttpConfigurer::disable)  // CSRF 보호 비활성화
            .headers(headers -> headers.frameOptions(FrameOptionsConfig::disable))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/h2-console/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/api/v1/mock/**", "/user/register", "/login")  // 로그인 및 회원가입 URL은 인증 없이 접근 허용
                    .permitAll()
                    .requestMatchers("/user/home")  // 홈 페이지는 로그인한 사용자만 접근 가능
                    .authenticated()
                    .requestMatchers("/user/my-page").authenticated()  // 마이 페이지도 로그인한 사용자만 접근 가능
                    .anyRequest().authenticated()  // 그 외 모든 요청은 인증 필요
            )
            .formLogin()
            .loginPage("/user/loginPage")
            .loginProcessingUrl("/login")
            .defaultSuccessUrl("/user/home", true)  // 로그인 성공 후 JSON 응답 반환
            .failureUrl("/login?error=true")   // 로그인 실패 후 처리
            .permitAll()
            .and()

            .logout()
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout")
            .invalidateHttpSession(true)  // 세션 무효화
            .deleteCookies("JSESSIONID")  // 쿠키 삭제
            .permitAll();

    return http.build();
  }

  @Autowired
  public void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(myUserDetailsService).passwordEncoder(passwordEncoder);  // PasswordEncoder를 사용
  }

}
