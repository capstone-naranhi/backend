package naranhi.backend.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import naranhi.backend.auth.LoginUser;
import naranhi.backend.auth.SessionUser;
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
            @LoginUser SessionUser loginUser,
            @PathVariable Long notificationId
    ) {
        return ApiResponse.ok(notificationService.getNotificationDetail(notificationId, loginUser.getId()));
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<Void> readNotification(
            @LoginUser SessionUser loginUser,
            @PathVariable Long notificationId
    ) {
        notificationService.readNotification(notificationId, loginUser.getId());
        return ApiResponse.ok(null);
    }

    @GetMapping("/unread-count")
    public ApiResponse<NotificationResponse.UnreadCount> getUnreadCount(
            @LoginUser SessionUser loginUser
    ) {
        return ApiResponse.ok(notificationService.getUnreadCount(loginUser.getId()));
    }

    @GetMapping
    public ApiResponse<NotificationResponse.NotificationList> getNotificationList(
            @LoginUser SessionUser loginUser,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) Long cursorId
    ) {
        return ApiResponse.ok(notificationService.getNotificationList(loginUser.getId(), type, cursorId));
    }
}
