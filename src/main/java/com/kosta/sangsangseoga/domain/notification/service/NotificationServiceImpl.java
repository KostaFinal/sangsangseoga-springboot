package com.kosta.sangsangseoga.domain.notification.service;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.notification.dto.NotificationDto;
import com.kosta.sangsangseoga.domain.notification.dto.NotificationListResponseDto;
import com.kosta.sangsangseoga.domain.notification.entity.Notification;
import com.kosta.sangsangseoga.domain.notification.exception.NotificationErrorCode;
import com.kosta.sangsangseoga.domain.notification.realtime.NotificationStreamPublisher;
import com.kosta.sangsangseoga.domain.notification.repository.NotificationRepository;
import com.kosta.sangsangseoga.global.event.AfterCommitTask;
import com.kosta.sangsangseoga.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationStreamPublisher notificationStreamPublisher;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public NotificationListResponseDto getNotifications(Long memberId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByMember_IdOrderByCreatedAtDesc(memberId, pageable);

        List<NotificationDto> items = notifications.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return NotificationListResponseDto.builder()
                .items(items)
                .totalCount(notifications.getTotalElements())
                .page(notifications.getNumber())
                .hasNext(notifications.hasNext())
                .build();
    }

    @Override
    public void markAsRead(Long memberId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        // 다른 회원의 알림 존재 여부가 드러나지 않도록 본인 소유가 아니면 NOT_FOUND로 동일하게 처리한다.
        if (!notification.getMember().getId().equals(memberId)) {
            throw new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
        }

        notification.markAsRead();
    }

    @Override
    public void markAllAsRead(Long memberId) {
        notificationRepository.markAllAsRead(memberId);
    }

    @Override
    public void deleteAll(Long memberId) {
        notificationRepository.deleteAllByMember_Id(memberId);
    }

    @Override
    public void notify(Member member, String content) {
        Notification notification = notificationRepository.save(Notification.builder()
                .member(member)
                .content(content)
                .build());

        // Redis 발행은 롤백이 안 되므로, 이 트랜잭션(신고 처리 등 호출부 트랜잭션)이 실제로 커밋된
        // 뒤에만 실시간 이벤트를 내보낸다. 롤백되면 알림 자체가 없던 일이 되므로 발행도 하지 않는다.
        Long notificationId = notification.getId();
        Long memberId = member.getId();
        LocalDateTime createdAt = notification.getCreatedAt();
        eventPublisher.publishEvent(new AfterCommitTask(this, () ->
                notificationStreamPublisher.publish(notificationId, memberId, content, createdAt)));
    }

    private NotificationDto toDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .text(notification.getContent())
                .createdAt(notification.getCreatedAt())
                .read(notification.isRead())
                .build();
    }
}
