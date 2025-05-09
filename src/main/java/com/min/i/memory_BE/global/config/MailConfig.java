package com.min.i.memory_BE.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

@Configuration
public class MailConfig {
    private static final Logger logger = LoggerFactory.getLogger(MailConfig.class);

    // 네이버 메일 환경변수 (선택적)
    @Value("${NAVER_MAIL_USERNAME:#{null}}")
    private String naverUsername;

    @Value("${NAVER_MAIL_PASSWORD:#{null}}")
    private String naverPassword;

    // 구글 메일 환경변수
    @Value("${GMAIL_MAIL_USERNAME}")
    private String gmailUsername;

    @Value("${GMAIL_MAIL_PASSWORD}")
    private String gmailPassword;

    @Bean(name = "naverMailSender")
    public JavaMailSender naverMailSender() {
        // 네이버 메일 정보가 없으면 Gmail 사용
        if (naverUsername == null || naverPassword == null) {
            logger.warn("네이버 메일 정보가 없어 Gmail을 사용합니다.");
            return gmailMailSender();
        }
        
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.naver.com");
        mailSender.setPort(465);
        mailSender.setUsername(naverUsername);
        mailSender.setPassword(naverPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");

        return mailSender;
    }

    @Bean(name = "gmailMailSender")
    public JavaMailSender gmailMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(465);
        mailSender.setUsername(gmailUsername);
        mailSender.setPassword(gmailPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");

        return mailSender;
    }

    public String getFromEmail(String email) {
        if (email.endsWith("@gmail.com") || naverUsername == null) {
            return gmailUsername;
        } else if (email.endsWith("@naver.com") && naverUsername != null) {
            return naverUsername;
        } else {
            // 기본값으로 Gmail 사용
            return gmailUsername;
        }
    }
}

