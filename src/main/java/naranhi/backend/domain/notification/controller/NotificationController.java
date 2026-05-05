package naranhi.backend.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import naranhi.backend.domain.notification.dto.NotificationResponse;
import naranhi.backend.domain.notification.entity.NotificationType;
import naranhi.backend.domain.notification.service.NotificationService;
import naranhi.backend.global.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{notificationId}")
    public ApiResponse<NotificationResponse.NotificationDetail> getNotificationDetail(
            @PathVariable Long notificationId,
            @RequestParam Long memberId
    ) {
        return ApiResponse.ok(notificationService.getNotificationDetail(notificationId, memberId));
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<Void> readNotification(
            @PathVariable Long notificationId,
            @RequestParam Long memberId
    ) {
        notificationService.readNotification(notificationId, memberId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/unread-count")
    public ApiResponse<NotificationResponse.UnreadCount> getUnreadCount(
            @RequestParam Long memberId
    ) {
        return ApiResponse.ok(notificationService.getUnreadCount(memberId));
    }

    @GetMapping
    public ApiResponse<NotificationResponse.NotificationList> getNotificationList(
            @RequestParam Long memberId,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) Long cursorId
    ) {
        return ApiResponse.ok(notificationService.getNotificationList(memberId, type, cursorId));
    }
}
