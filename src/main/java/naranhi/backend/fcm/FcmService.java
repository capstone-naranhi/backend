package naranhi.backend.fcm;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naranhi.backend.domain.notification.entity.NotificationType;
import naranhi.backend.fcm.fcmToken.entity.FcmToken;
import naranhi.backend.fcm.fcmToken.repository.FcmTokenRepository;
import naranhi.backend.fcm.fcmToken.service.FcmTokenService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final FirebaseMessaging firebaseMessaging;
    private final FcmTokenRepository fcmTokenRepository;
    private final FcmTokenService fcmTokenService;

    /**
     * 회원에게 FCM 전송
     */
    public void sendToMember(Long memberId, FcmPayload payload, NotificationType type) {
        List<FcmToken> tokens = fcmTokenRepository.findActiveByMemberId(memberId);

        if (tokens.isEmpty()) {
            log.warn("활성 FCM 토큰 없음 - memberId: {}", memberId);
            return;
        }

        tokens.forEach(token -> send(token, payload, type));
    }

    public void sendToMembers(List<Long> memberIds, FcmPayload payload, NotificationType type) {
        memberIds.forEach(memberId -> sendToMember(memberId, payload, type));
    }

    private void send(FcmToken fcmToken, FcmPayload payload, NotificationType type) {
        Message message = Message.builder()
                .setToken(fcmToken.getToken())
                .setNotification(Notification.builder()
                        .setTitle(payload.title())
                        .setBody(payload.body())
                        .build()
                )
                .putAllData(payload.data())
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(
                                type == NotificationType.SAFETY
                                        ? AndroidConfig.Priority.HIGH
                                        : AndroidConfig.Priority.NORMAL
                        )
                        .build()
                )
                .build();

        try {
            String messageId = firebaseMessaging.send(message);
            log.info("FCM 전송 성공 - messageId: {}", messageId);

        } catch (FirebaseMessagingException e) {
            log.error("FCM 전송 실패 - error: {}", e.getMessagingErrorCode());

            if (MessagingErrorCode.UNREGISTERED.equals(e.getMessagingErrorCode())) {
                fcmTokenService.deactivateToken(fcmToken.getToken());
                log.info("만료된 FCM 토큰 비활성화 처리");
            }
        }
    }
}