package com.kosta.sangsangseoga.domain.notification.service;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.notification.dto.NotificationListResponseDto;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    NotificationListResponseDto getNotifications(Long memberId, Pageable pageable);

    void markAsRead(Long memberId, Long notificationId);

    void markAllAsRead(Long memberId);

    void deleteAll(Long memberId);

    /**
     * 알림 생성. 다른 도메인(신고 처리, 도서 발행 등)에서 특정 회원에게 알림을 보낼 때 호출한다.
     */
    void notify(Member member, String content);
}
