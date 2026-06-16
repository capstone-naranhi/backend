package naranhi.backend.mqtt.processor;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naranhi.backend.domain.device.entity.Device;
import naranhi.backend.domain.device.repository.DeviceRepository;
import naranhi.backend.domain.member.entity.Member;
import naranhi.backend.domain.member.repository.MemberRepository;
import naranhi.backend.domain.notification.entity.Notification;
import naranhi.backend.domain.notification.entity.NotificationRecipient;
import naranhi.backend.domain.notification.entity.NotificationType;
import naranhi.backend.domain.notification.entity.SafetyNotification;
import naranhi.backend.domain.notification.repository.NotificationRecipientRepository;
import naranhi.backend.domain.notification.repository.NotificationRepository;
import naranhi.backend.domain.notification.repository.SafetyNotificationRepository;
import naranhi.backend.domain.safety.entity.EventType;
import naranhi.backend.domain.safety.entity.SafetyEvent;
import naranhi.backend.domain.safety.repository.SafetyEventRepository;
import naranhi.backend.domain.safety.service.DangerStateService;
import naranhi.backend.fcm.FcmPayload;
import naranhi.backend.fcm.FcmService;
import naranhi.backend.log.service.MongoLogService;
import naranhi.backend.mqtt.dto.DangerEventMessage;
import naranhi.backend.s3.PresignedUrlService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DangerEventProcessor {

    private final DeviceRepository deviceRepository;
    private final MemberRepository memberRepository;
    private final SafetyEventRepository safetyEventRepository;
    private final NotificationRepository notificationRepository;
    private final SafetyNotificationRepository safetyNotificationRepository;
    private final NotificationRecipientRepository notificationRecipientRepository;
    private final FcmService fcmService;
    private final MongoLogService mongoLogService;
    private final DangerStateService dangerStateService;
    private final PresignedUrlService presignedUrlService;

    public void process(DangerEventMessage message) {
        Device device = deviceRepository.findByDeviceSerialNumber(message.deviceSerial())
                .orElseThrow(() -> {
                    log.warn("미등록 장치 위험 이벤트 - serial: {}", message.deviceSerial());
                    return new IllegalArgumentException("미등록 장치: " + message.deviceSerial());
                });

        EventType eventType;
        try {
            eventType = EventType.valueOf(message.eventType());
        } catch (IllegalArgumentException e) {
            log.error("알 수 없는 EventType - serial: {}, eventType: {}", message.deviceSerial(), message.eventType());
            return;
        }

        // END: Redis 위험 상태만 삭제, DB 저장/FCM 없음
        if ("END".equals(message.phase())) {
            dangerStateService.markEnd(message.deviceSerial());
            log.info("위험 종료 - serial: {}, eventType: {}", message.deviceSerial(), eventType);
            return;
        }

        // DB 저장 + FCM + 로그
        List<Long> memberIds = deviceRepository.findMemberIdsByDeviceSerialNumber(message.deviceSerial());
        if (memberIds.isEmpty()) {
            log.warn("연결된 회원 없음 - serial: {}", message.deviceSerial());
            return;
        }

        Long notifId = saveMysql(message, device, memberIds, eventType);

        FcmPayload payload = FcmPayload.ofSafety(eventType, device.getDeviceName(), notifId);
        fcmService.sendToMembers(memberIds, payload, NotificationType.SAFETY);

        mongoLogService.saveDangerEventLog(message);

        log.info("위험 감지 처리 완료 - serial: {}, eventType: {}, phase: {}, notifId: {}, 수신자: {}명",
                message.deviceSerial(), eventType, message.phase(), notifId, memberIds.size());
    }

    @Transactional
    public Long saveMysql(
            DangerEventMessage message,
            Device device,
            List<Long> memberIds,
            EventType eventType
    ) {
        LocalDateTime detectedAt = message.detectedAt() != null
                ? message.detectedAt().toLocalDateTime()
                : LocalDateTime.now();

        SafetyEvent safetyEvent = safetyEventRepository.save(
                SafetyEvent.builder()
                        .device(device)
                        .eventType(eventType)
                        .severity(eventType.getDefaultSeverity())
                        .confidence(message.confidence() != null
                                ? BigDecimal.valueOf(message.confidence())
                                : null)
                        .durationSecond(message.duration())
                        .snapshotUrl(presignedUrlService.buildPublicUrl(message.snapshotUrl()))
                        .videoUrl(presignedUrlService.buildPublicUrl(message.videoUrl()))
                        .detectedAt(detectedAt)
                        .build()
        );

        device.updateLastEventAt(LocalDateTime.now());
        deviceRepository.save(device);

        Notification notification = notificationRepository.save(
                Notification.builder()
                        .type(NotificationType.SAFETY)
                        .sentAt(LocalDateTime.now())
                        .build()
        );

        safetyNotificationRepository.save(
                SafetyNotification.builder()
                        .notification(notification)
                        .safetyEvent(safetyEvent)
                        .device(device)
                        .eventType(eventType)
                        .severity(eventType.getDefaultSeverity())
                        .build()
        );

        List<Member> members = memberRepository.findAllById(memberIds);
        notificationRecipientRepository.saveAll(
                members.stream()
                        .map(member -> NotificationRecipient.builder()
                                .notification(notification)
                                .member(member)
                                .isRead(false)
                                .isSent(false)
                                .build()
                        )
                        .toList()
        );

        return notification.getId();
    }

}
