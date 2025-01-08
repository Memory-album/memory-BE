package com.min.i.memory_BE.domain.user.service;

import com.min.i.memory_BE.domain.user.dto.UserRegisterDto;
import com.min.i.memory_BE.global.config.MailConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class EmailService {

    @Autowired
    @Qualifier("gmailMailSender")
    private JavaMailSender gmailMailSender; // Gmail 전용 JavaMailSender

    @Autowired
    @Qualifier("naverMailSender")
    private JavaMailSender naverMailSender; // 네이버 전용 JavaMailSender

    @Autowired
    private MailConfig mailConfig;  // MailConfig를 주입받아 fromEmail을 동적으로 설정

    @Value("${jwt.secret}")
    private String secretKey;  // JWT 서명에 사용할 비밀키

    // 이메일 인증 코드 발송
    public String sendVerificationCode(UserRegisterDto userRegisterDto) {
        try {

            // 인증 코드 생성 (6자리 숫자)
            String verificationCode = generateVerificationCode();

            // 인증 코드와 유효 기간 설정
            LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(15); // 15분 유효

            // JWT 생성 (이메일, 인증 코드, 만료 시간 포함)
            String jwt = Jwts.builder()
                    .claim("email", userRegisterDto.getEmail())
                    .claim("emailVerificationCode", verificationCode)
                    .claim("expirationTime", expirationTime.toString())
                    .claim("isEmailVerified", false)  // 이메일 인증 여부는 false로 설정
                    .signWith(Keys.hmacShaKeyFor(getSecretKey().getBytes()), SignatureAlgorithm.HS256)
                    .compact();

            // 이메일 전송
            sendEmail(userRegisterDto.getEmail(), verificationCode);

            return jwt; // 생성된 JWT 반환

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 이메일 전송
    private void sendEmail(String email, String verificationCode) {
        // 이메일 내용 설정 (HTML)
        try {
            MimeMessage message = getMailSender(email).createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 기본 텍스트와 함께 HTML 내용을 추가
            helper.setFrom(mailConfig.getFromEmail(email));
            helper.setTo(email);
            helper.setSubject("Min:i 이메일 인증 코드");

            String content = "<html>"
                    + "<head><style>"
                    + "body { font-family: Arial, sans-serif; color: #333333; background-color: #f4f4f4; padding: 20px; }"
                    + ".container { width: 100%; max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 20px; border-radius: 8px; }"
                    + ".header { background-color: #008cba; color: white; padding: 10px; text-align: center; border-radius: 8px 8px 0 0; }"
                    + ".footer { text-align: center; margin-top: 20px; font-size: 12px; color: #777777; }"
                    + ".button { background-color: #008cba; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; font-size: 16px; }"
                    + "</style></head>"
                    + "<body>"
                    + "<div class='container'>"
                    + "<div class='header'><h2>Min:i 인증 코드</h2></div>"
                    + "<p>안녕하세요! 아래 인증 코드를 입력하여 이메일 인증을 완료해 주세요.</p>"
                    + "<h3 style='text-align: center; color: #008cba;'>"
                    + verificationCode
                    + "</h3>"
                    + "<p style='text-align: center;'>인증 코드 유효시간: 15분</p>"
                    + "<div class='footer'>"
                    + "<p>감사합니다. Min:i 서비스</p>"
                    + "</div>"
                    + "</div>"
                    + "</body>"
                    + "</html>";

            helper.setText(content, true);  // true로 설정하여 HTML 이메일 전송

            // 메일 서버에 이메일 전송
            getMailSender(email).send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 인증 코드 생성
    private String generateVerificationCode() {
        return String.valueOf((int)(Math.random() * 1000000));  // 6자리 랜덤 숫자
    }

    // 이메일 도메인에 맞는 메일 서버 선택
    private JavaMailSender getMailSender(String email) {
        if (email.endsWith("@gmail.com")) {
            return gmailMailSender;
        } else if (email.endsWith("@naver.com")) {
            return naverMailSender;
        } else {
            throw new IllegalArgumentException("지원되지 않는 이메일 도메인입니다.");
        }
    }

    // JWT 비밀키를 Base64 URL-safe로 인코딩
    private String getSecretKey() {
        return Base64.getEncoder().encodeToString(secretKey.getBytes());
    }
}

