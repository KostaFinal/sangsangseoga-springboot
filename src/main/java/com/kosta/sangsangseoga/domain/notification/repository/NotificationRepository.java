package com.kosta.sangsangseoga.domain.notification.repository;

import com.kosta.sangsangseoga.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByMember_IdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.member.id = :memberId AND n.isRead = false")
    void markAllAsRead(@Param("memberId") Long memberId);

    void deleteAllByMember_Id(Long memberId);
}
