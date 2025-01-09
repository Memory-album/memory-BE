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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

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
                .requestMatchers(
                        "/h2-console/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/api/v1/mock/**",
                        "/register/send-verification-code",
                        "/register/verify-email",
                        "/register/complete-register",
                        "/user/loginPage",
                        "/oauth/callback",
                        "/oauth/login",
                        "/auth/login"
                ).permitAll() // 인증 없이 접근 허용
                .requestMatchers("/user/update", "/user/delete", 
                        "/user/activate", "/user/deactivate",
                        "/auth/logout", "/oauth/logout").authenticated() // 인증된 사용자만 접근 가능
                .anyRequest().authenticated() // 나머지 요청은 인증 필요
                .and()
                    .logout()
                    .logoutUrl("/auth/logout") // 일반 로그아웃 URL
                    .logoutSuccessUrl("/user/loginPage") // 일반 로그아웃 성공 후 리다이렉트 URL
                    .invalidateHttpSession(true) // 세션 무효화
                    .deleteCookies("JSESSIONID", "jwtToken", "refreshToken") // 쿠키 삭제
                    .permitAll()
                .and()
                    .logout()
                    .logoutRequestMatcher(new AntPathRequestMatcher("/oauth/logout", "GET")) // GET 메서드 허용
                    .logoutSuccessHandler((request, response, authentication) -> {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().write("OAuth logout success");
                    })
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID", "jwtToken", "refreshToken")
                .and()
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .headers()
                .frameOptions().sameOrigin(); // H2 콘솔 허용

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
