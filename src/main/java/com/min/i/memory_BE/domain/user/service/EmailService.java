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
                    .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
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

            // 네이버 메일인 경우 다른 템플릿 사용
            String content = email.endsWith("@naver.com") 
                ? getNaverEmailTemplate(verificationCode)
                : getDefaultEmailTemplate(verificationCode);

            helper.setText(content, true);
            getMailSender(email).send(message);
        } catch (Exception e) {
            throw new RuntimeException("이메일 전송 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 네이버 메일용 템플릿
    private String getNaverEmailTemplate(String verificationCode) {
        return "<div style='max-width: 600px; margin: 20px auto; padding: 20px; word-break: keep-all; line-height: 1.5;'>"
                + "<div style='margin-bottom: 20px; padding-bottom: 20px; border-bottom: 1px solid #ccc;'>"
                + "<h2 style='margin: 0; color: #333;'>Min:i 이메일 인증</h2>"
                + "</div>"
                + "<div style='margin-bottom: 30px;'>"
                + "<p style='margin: 10px 0;'>안녕하세요!</p>"
                + "<p style='margin: 10px 0;'>Min:i 서비스 이메일 인증을 위한 인증 코드입니다.</p>"
                + "<div style='font-size: 24px; padding: 20px; margin: 20px 0; background: #f8f8f8; text-align: center; border-radius: 8px;'>"
                + "<strong style='letter-spacing: 3px;'>" + verificationCode + "</strong>"
                + "</div>"
                + "<p style='margin: 10px 0;'>위 인증 코드를 입력하여 이메일 인증을 완료해 주세요.</p>"
                + "<p style='margin: 10px 0; color: #ff6b6b; font-size: 14px;'>* 인증 코드는 발급 후 15분 동안만 유효합니다.</p>"
                + "</div>"
                + "<div style='border-top: 1px solid #ccc; padding-top: 20px; font-size: 13px; color: #777;'>"
                + "<p style='margin: 5px 0;'>본 메일은 발신 전용입니다.</p>"
                + "<p style='margin: 5px 0;'>&copy; " + LocalDateTime.now().getYear() + " Min:i. All rights reserved.</p>"
                + "</div>"
                + "</div>";
    }

    // 기존 템플릿은 getDefaultEmailTemplate로 이름 변경
    private String getDefaultEmailTemplate(String verificationCode) {
        return "<html>"
                + "<head>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<style>"
                + "* { margin: 0; padding: 0; box-sizing: border-box; }"
                + "body { font-family: 'Noto Sans KR', sans-serif; color: #333333; background-color: #f4f4f4; padding: 20px; word-break: keep-all; line-height: 1.6; }"
                + ".container { width: 100%; max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }"
                + ".header { background-color: #4CAF50; color: white; padding: 15px; text-align: center; border-radius: 8px 8px 0 0; }"
                + ".header h2 { margin: 0; font-size: 20px; }"
                + ".content { padding: 20px; }"
                + ".content p { margin: 10px 0; }"
                + ".verification-code { text-align: center; font-size: 24px; font-weight: bold; color: #4CAF50; padding: 15px; margin: 20px 0; background-color: #f8f8f8; border-radius: 4px; letter-spacing: 3px; }"
                + ".warning { color: #ff6b6b; font-size: 14px; margin: 15px 0; }"
                + ".footer { text-align: center; margin-top: 20px; padding-top: 20px; border-top: 1px solid #eeeeee; font-size: 12px; color: #777777; }"
                + ".footer p { margin: 5px 0; }"
                + "@media screen and (max-width: 480px) {"
                + "  body { padding: 10px; }"
                + "  .container { padding: 15px; }"
                + "  .content { padding: 15px 10px; }"
                + "}"
                + "</style></head>"
                + "<body>"
                + "<div class='container'>"
                + "<div class='header'><h2>Min:i 이메일 인증</h2></div>"
                + "<div class='content'>"
                + "<p>안녕하세요!</p>"
                + "<p>Min:i 서비스 이메일 인증을 위한 인증 코드입니다.</p>"
                + "<div class='verification-code'>" + verificationCode + "</div>"
                + "<p>위 인증 코드를 입력하여 이메일 인증을 완료해 주세요.</p>"
                + "<p class='warning'>* 인증 코드는 발급 후 15분 동안만 유효합니다.</p>"
                + "</div>"
                + "<div class='footer'>"
                + "<p>본 메일은 발신 전용입니다.</p>"
                + "<p>&copy; " + LocalDateTime.now().getYear() + " Min:i. All rights reserved.</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
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
}

