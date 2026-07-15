package com.kosta.sangsangseoga.domain.notification.controller;

import com.kosta.sangsangseoga.domain.notification.dto.NotificationListResponseDto;
import com.kosta.sangsangseoga.domain.notification.service.NotificationService;
import com.kosta.sangsangseoga.global.common.ApiResponse;
import com.kosta.sangsangseoga.global.config.ApiErrorCodes;
import com.kosta.sangsangseoga.global.security.AuthenticationHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notification", description = "내 알림 조회/읽음 처리")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "내 알림 목록 조회", description = "로그인 회원의 알림을 최신순으로 페이지네이션 조회한다.")
    @ApiErrorCodes({})
    @GetMapping
    public ResponseEntity<ApiResponse<NotificationListResponseDto>> getNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long memberId = AuthenticationHelper.resolveMemberId(authentication);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(notificationService.getNotifications(memberId, pageable)));
    }

    @Operation(summary = "알림 읽음 처리", description = "알림 하나를 읽음 처리한다. 본인 알림이 아니면 404로 처리한다.")
    @ApiErrorCodes({"NOTIFICATION_NOT_FOUND"})
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(Authentication authentication, @PathVariable Long id) {
        Long memberId = AuthenticationHelper.resolveMemberId(authentication);
        notificationService.markAsRead(memberId, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "전체 알림 읽음 처리")
    @ApiErrorCodes({})
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Authentication authentication) {
        Long memberId = AuthenticationHelper.resolveMemberId(authentication);
        notificationService.markAllAsRead(memberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "전체 알림 삭제")
    @ApiErrorCodes({})
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAll(Authentication authentication) {
        Long memberId = AuthenticationHelper.resolveMemberId(authentication);
        notificationService.deleteAll(memberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
