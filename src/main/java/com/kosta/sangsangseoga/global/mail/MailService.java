package com.kosta.sangsangseoga.global.mail;

import com.kosta.sangsangseoga.global.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    public void sendPasswordResetEmail(String to, String token) {
        String link = appProperties.getFrontendUrl() + "/reset-password?token=" + token;
        send(to, "[상상서가] 비밀번호 재설정 안내",
                "아래 링크에서 비밀번호를 재설정해 주세요. (30분간 유효)\n" + link);
    }

    public void sendGuardianConsentEmail(String to, Long consentId, String token) {
        String link = appProperties.getFrontendUrl()
                + "/guardian-consent?consentId=" + consentId + "&token=" + token;
        send(to, "[상상서가] 보호자 동의 요청",
                "자녀의 회원가입 동의를 위해 아래 링크를 확인해 주세요. (7일간 유효)\n" + link);
    }

    private void send(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(appProperties.getMailFrom());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
