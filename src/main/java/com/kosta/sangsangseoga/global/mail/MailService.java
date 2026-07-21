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

    private static final String DIVIDER = "\n----------------------------------------\n";
    private static final String SIGNATURE = "\n\n감사합니다.\n상상서가 드림";

    public void sendPasswordResetEmail(String to, String memberNickname, String token) {
        String link = appProperties.getFrontendUrl() + "/reset-password?token=" + token;
        send(to, "[상상서가] 비밀번호 재설정 안내",
                memberNickname + "님, 안녕하세요.\n"
                        + "상상서가 계정의 비밀번호 재설정 요청이 접수되어 안내드립니다." + DIVIDER
                        + "아래 링크로 접속하시면 새 비밀번호를 입력하는 페이지로 이동합니다.\n"
                        + "링크를 여는 것만으로는 비밀번호가 변경되지 않으며, 페이지에서 새 비밀번호를 "
                        + "입력하고 저장해야 실제로 반영됩니다.\n\n"
                        + link + "\n\n"
                        + "이 링크는 발송 시점으로부터 30분간 유효하며, 시간이 지나면 재설정을 다시 요청해야 합니다."
                        + DIVIDER
                        + "본인이 요청하지 않으셨다면 이 메일은 무시하셔도 안전합니다.\n"
                        + "다만 본인 요청이 아닌 메일을 계속 받으신다면, 계정 보호를 위해 비밀번호를 변경해 주세요."
                        + SIGNATURE);
    }

    public void sendGuardianConsentEmail(String to, String memberNickname, Long consentId, String token) {
        String link = appProperties.getFrontendUrl()
                + "/guardian-consent?consentId=" + consentId + "&token=" + token;
        send(to, "[상상서가] " + memberNickname + "님의 회원가입 보호자 동의 요청",
                "안녕하세요.\n"
                        + memberNickname + "님이 상상서가 회원가입을 위해 법정대리인(보호자)의 동의를 요청하셨습니다."
                        + DIVIDER
                        + "아래 링크로 접속하시면 가입 신청 내용을 확인하실 수 있습니다.\n"
                        + "링크를 여는 것만으로는 동의 처리가 되지 않으며, 내용을 확인하신 뒤 페이지에서 "
                        + "[동의] 또는 [거절] 버튼을 직접 선택하셔야 최종 반영됩니다.\n\n"
                        + link + "\n\n"
                        + "이 링크는 발송 시점으로부터 7일간 유효하며, 기간이 지나면 가입 신청을 다시 받아야 합니다."
                        + DIVIDER
                        + "본인과 관련 없는 요청이라면 이 메일은 무시하셔도 됩니다."
                        + SIGNATURE);
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
