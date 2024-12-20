package com.min.i.memory_BE.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    // 네이버 메일 환경변수
    @Value("${NAVER_MAIL_USERNAME}")
    private String naverUsername;

    @Value("${NAVER_MAIL_PASSWORD}")
    private String naverPassword;

    // 구글 메일 환경변수
    @Value("${GMAIL_MAIL_USERNAME}")
    private String gmailUsername;

    @Value("${GMAIL_MAIL_PASSWORD}")
    private String gmailPassword;


    @Bean(name = "naverMailSender")
    public JavaMailSender naverMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.naver.com");
        mailSender.setPort(465);
        mailSender.setUsername(naverUsername);  // 환경변수로 설정된 네이버 이메일
        mailSender.setPassword(naverPassword);  // 환경변수로 설정된 네이버 이메일 비밀번호

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
        mailSender.setUsername(gmailUsername);  // 환경변수로 설정된 Gmail 이메일
        mailSender.setPassword(gmailPassword);  // 환경변수로 설정된 Gmail 앱 비밀번호

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");

        return mailSender;
    }
}

