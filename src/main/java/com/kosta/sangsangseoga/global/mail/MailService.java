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

    public void sendPasswordResetEmail(String to, String memberNickname, String token) {
        String link = appProperties.getFrontendUrl() + "/reset-password?token=" + token;
        send(to, "[상상서가] 비밀번호 재설정 안내",
                memberNickname + "님, 상상서가 계정의 비밀번호 재설정을 요청받았습니다.\n\n"
                        + "아래 링크를 클릭하면 새 비밀번호를 입력하는 페이지로 이동합니다. "
                        + "링크를 여는 것만으로는 비밀번호가 바뀌지 않으며, 페이지에서 새 비밀번호를 "
                        + "입력하고 저장해야 실제로 변경됩니다.\n\n"
                        + link + "\n\n"
                        + "이 링크는 30분간 유효하며, 기간이 지나면 다시 요청해야 합니다.\n"
                        + "본인이 요청한 적이 없다면 이 메일을 무시하셔도 되며, "
                        + "계속 이런 메일을 받는다면 비밀번호를 변경해 주세요.");
    }

    public void sendGuardianConsentEmail(String to, String memberNickname, Long consentId, String token) {
        String link = appProperties.getFrontendUrl()
                + "/guardian-consent?consentId=" + consentId + "&token=" + token;
        send(to, "[상상서가] " + memberNickname + "님의 회원가입 보호자 동의 요청",
                memberNickname + "님이 상상서가 회원가입을 위해 법정대리인(보호자) 동의를 요청했습니다.\n\n"
                        + "아래 링크를 클릭하면 요청 내용을 확인하는 페이지로 이동합니다. "
                        + "링크를 여는 것만으로는 동의 처리가 되지 않으며, 페이지에서 내용을 확인한 뒤 "
                        + "[동의] 또는 [거절] 버튼을 직접 눌러야 최종 처리됩니다.\n\n"
                        + link + "\n\n"
                        + "이 링크는 7일간 유효하며, 기간이 지나면 다시 요청해야 합니다.\n"
                        + "본인이 요청한 적이 없다면 이 메일을 무시하셔도 됩니다.");
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
