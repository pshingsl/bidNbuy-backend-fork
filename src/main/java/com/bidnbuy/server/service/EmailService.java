package com.bidnbuy.server.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    //메일 템플릿 적용
    private MimeMessage createMessage(String to, String ePw) throws MessagingException, UnsupportedEncodingException {
        log.info("보내는 대상 : " + to);
        log.info("인증 번호 : " + ePw);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8"); // MimeMessageHelper 사용

        helper.setTo(to);
        helper.setSubject("[Bid-&-Buy] 회원가입 인증 코드: "); // 메일 제목

        String msg = String.format(
                "<div style=\" width: 540px; height: 600px; border-top: 4px solid #8322BF; margin: 100px auto; padding: 30px 0; box-sizing: border-box;\">" +
                        "<h1 style=\"margin: 0; padding: 0 5px; font-size: 28px; font-weight: 400;\">" +
                        "<span style=\"font-size: 30px; margin: 0 0 10px 3px; font-weight: 800;\">Bid<span style=\"color: #8322BF;\">&</span>Buy</span><br />" +
                        "<span style=\"color: #8322BF;\">이메일 인증번호</span> 안내입니다." +
                        "</h1>" +
                        "<p style=\"font-size: 16px; line-height: 26px; margin-top: 50px; padding: 0 5px;\">" +
                        "안녕하세요.<br />" +
                        "회원가입을 위한 이메일 인증 코드가 발급되었습니다.<br />" +
                        "</p>" +
                        "<p style=\"font-size: 16px; margin: 40px 5px 20px; line-height: 28px;\">" +
                        "인증 번호: <br />" +
                        "<span style=\"font-size: 24px;\">%s</span>" + // 인증 번호 (ePw) 삽입 위치
                        "</p>" +
                        "</div>", ePw // String.format을 사용하여 ePw(인증 번호) 삽입
        );

        // HTML 내용 설정 (true 설정으로 HTML 활성화)
        helper.setText(msg, true);

        // 보내는 사람 설정
        helper.setFrom(new InternetAddress(fromEmail, "Bid-&-Buy 관리자"));

        return message;
    }


    //2차 인증 코드 생성
    public String createVerificationCode(){
        //랜덤 6 자리 숫자 생성하기
        int code = (int)(Math.random()*899999)+ 100000;
        return String.valueOf(code);
    }

    public void sendVerificationEmail(String toEmail, String verificationCode) {
        try {
            // 1. HTML 메일 메시지 생성
            MimeMessage message = createMessage(toEmail, verificationCode);

            // 2. 메일 전송
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("인증 이메일 전송 중 오류 발생: {}", e.getMessage());
            // 예외 처리 로직 추가 (예: RuntimeException throw)
            throw new RuntimeException("이메일 전송 실패", e);
        }
    }

//    //이메일 전송하기
//    public void sendVerificationEmail(String toEmail, String subject, String text){
//        SimpleMailMessage message = new SimpleMailMessage();
//
//        //application.properties에 설정한 username이 발신자
//        message.setFrom(fromEmail);
//        message.setTo(toEmail); //수신자
//        message.setSubject(subject); //제목
//        message.setText(text);//본문 내용
//
//        mailSender.send(message);
//    }

    //임시 비밀번호 이메일 발송
//    public void sendTempPasswordEmail(String toEmail, String tempPassword){
//        SimpleMailMessage message = new SimpleMailMessage();
//
//        message.setFrom(fromEmail);
//        message.setTo(toEmail);
//        message.setSubject("[BID-n-BUY] 비밀번호 재설정 임시 비밀번호");
//        String text = String.format(
//            "요청하신 임시 비밀번호는 **%s** 입니다.\n\n" +
//            "임시 비밀번호는 10분간 유효합니다.", tempPassword
//        );
//        message.setText(text);
//        mailSender.send(message);
//    }
    public void sendTempPasswordEmail(String toEmail, String tempPassword){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(new InternetAddress(fromEmail, "Bid-&-Buy 관리자"));
            helper.setTo(toEmail);
            helper.setSubject("[Bid-&-Buy] 비밀번호 재설정 임시 비밀번호");

            // HTML 내용으로 변경
            String htmlContent = String.format(
                    "<div style=\" width: 540px; height: 600px; border-top: 4px solid #8322BF; margin: 100px auto; padding: 30px 0; box-sizing: border-box;\">" +
                            "<h1 style=\"margin: 0; padding: 0 5px; font-size: 28px; font-weight: 400;\">" +
                            "<span style=\"font-size: 30px; margin: 0 0 10px 3px; font-weight: 800;\">Bid<span style=\"color: #8322BF;\">&</span>Buy</span><br />" +
                            "<span style=\"color: #8322BF;\">임시 비밀번호</span> 안내입니다." +
                            "</h1>" +
                            "<p style=\"font-size: 16px; line-height: 26px; margin-top: 50px; padding: 0 5px;\">" +
                            "안녕하세요.<br />" +
                            "요청하신 임시 비밀번호가 생성되었습니다.<br />" +
                            "</p>" +
                            "<p style=\"font-size: 16px; margin: 40px 5px 20px; line-height: 28px;\">" +
                            "임시 비밀번호: <br />" +
                            "<span style=\"font-size: 24px;\">%s</span>" + // 임시 비밀번호(tempPassword) 삽입 위치
                            "</p>" +
                            "</div>", tempPassword
            );

            helper.setText(htmlContent, true); // true로 HTML 활성화

            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("임시 비밀번호 이메일 전송 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("임시 비밀번호 이메일 전송 실패", e);
        }
    }

    // 관리자 임시 비번 발송
    public void sendTempPasswordEmailForAdmin(String toEmail, String tempPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(new InternetAddress(fromEmail, "Bid-&-Buy 관리자"));
            helper.setTo(toEmail);
            helper.setSubject("[Bid-&-Buy] [관리자] 비밀번호 재설정 임시 비밀번호");

            String htmlContent = String.format(
                    "<div style=\" width: 540px; height: 600px; border-top: 4px solid #8322BF; margin: 100px auto; padding: 30px 0; box-sizing: border-box;\">" +
                            "<h1 style=\"margin: 0; padding: 0 5px; font-size: 28px; font-weight: 400;\">" +
                            "<span style=\"font-size: 30px; margin: 0 0 10px 3px; font-weight: 800;\">Bid<span style=\"color: #8322BF;\">&</span>Buy</span><br />" +
                            "<span style=\"color: #8322BF;\">[관리자] 임시 비밀번호</span> 안내입니다." +
                            "</h1>" +
                            "<p style=\"font-size: 16px; line-height: 26px; margin-top: 50px; padding: 0 5px;\">" +
                            "관리자님 안녕하세요.<br />" +
                            "요청하신 임시 비밀번호가 생성되었습니다.<br />" +
                            "</p>" +
                            "<p style=\"font-size: 16px; margin: 40px 5px 20px; line-height: 28px;\">" +
                            "임시 비밀번호: <br />" +
                            "<span style=\"font-size: 24px;\">%s</span>" +
                            "</p>" +
                            "<p style=\"font-size: 14px; color: #999; margin-top: 30px; padding: 0 5px;\">" +
                            "※ 임시 비밀번호는 10분간 유효합니다." +
                            "</p>" +
                            "</div>", tempPassword
            );

            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("관리자 임시 비밀번호 이메일 전송 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("관리자 임시 비밀번호 이메일 전송 실패", e);
        }
    }
}
