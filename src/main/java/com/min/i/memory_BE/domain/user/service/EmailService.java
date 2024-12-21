package com.min.i.memory_BE.domain.user.service;

import com.min.i.memory_BE.domain.user.dto.UserRegisterDto;
import com.min.i.memory_BE.domain.user.entity.User;
import com.min.i.memory_BE.domain.user.enums.UserMailStatus;
import com.min.i.memory_BE.domain.user.repository.UserRepository;
import com.min.i.memory_BE.global.config.MailConfig;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
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
    private UserRepository userRepository; // UserRepository를 사용해 이메일 인증 코드 및 유효 기간을 저장

    @Autowired
    private MailConfig mailConfig;  // MailConfig를 주입받아 fromEmail을 동적으로 설정

    // 이메일 인증 코드 발송
    public boolean sendVerificationCode(UserRegisterDto userRegisterDto) {
        try {

            // 이메일로 사용자 검색 (기존 사용자 확인)
            User user = userRepository.findByEmail(userRegisterDto.getEmail())
                    .orElse(null); // 임시 사용자가 없다면 null 반환

            if (user != null) {
                // 기존 사용자가 있을 경우, 최종 가입된 사용자인지 확인
                if (user.getMailStatus() == UserMailStatus.REGISTERED) {
                    throw new IllegalArgumentException("이메일은 이미 가입이 완료되었습니다.");
                }

                // 미인증 사용자라면 삭제하고 새로 임시 사용자로 저장
                if (user.getMailStatus() == UserMailStatus.UNVERIFIED) {
                    userRepository.delete(user); // 기존 미인증 사용자 삭제
                }
            }

            // 새로 임시 사용자로 저장 (이메일 인증을 위해 임시로 데이터 저장)

            // 인증 코드 생성 (6자리 숫자)
            String verificationCode = generateVerificationCode();

            // 인증 코드와 유효 기간 설정
            LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(15); // 15분 유효

            user = User.createTemporaryUser(userRegisterDto.getEmail(), verificationCode, expirationTime);

            // 데이터베이스에 임시 사용자 저장
            userRepository.save(user);

            // 이메일 내용 설정 (HTML)
            MimeMessage message = getMailSender(userRegisterDto.getEmail()).createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 기본 텍스트와 함께 HTML 내용을 추가
            helper.setFrom(mailConfig.getFromEmail(userRegisterDto.getEmail()));
            helper.setTo(userRegisterDto.getEmail());
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
            getMailSender(userRegisterDto.getEmail()).send(message);


            return true; // 이메일 전송 성공

        } catch (Exception e) {
            // 다른 예외 처리
            System.err.println("알 수 없는 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();  // 예외의 StackTrace 출력
            return false; // 기타 예외 발생 시 false 반환
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
}

