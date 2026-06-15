package naranhi.backend.home.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import naranhi.backend.domain.device.entity.Device;
import naranhi.backend.domain.device.entity.MemberDevice;
import naranhi.backend.domain.device.repository.MemberDeviceRepository;
import naranhi.backend.domain.safety.dto.DangerState;
import naranhi.backend.domain.safety.entity.EventType;
import naranhi.backend.domain.safety.repository.SafetyEventRepository;
import naranhi.backend.domain.safety.service.DangerStateService;
import naranhi.backend.home.dto.HomeResponse;
import naranhi.backend.domain.notification.dto.NotificationResponse;
import naranhi.backend.domain.notification.repository.NotificationRecipientRepository;
import naranhi.backend.domain.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private static final int RECENT_NOTIFICATION_LIMIT = 2;

    private final MemberDeviceRepository memberDeviceRepository;
    private final SafetyEventRepository safetyEventRepository;
    private final NotificationRecipientRepository notificationRecipientRepository;
    private final NotificationService notificationService;
    private final DangerStateService dangerStateService;

    public HomeResponse.Home getHome(Long memberId) {
        List<Device> devices = memberDeviceRepository.findAllWithDeviceByMemberId(memberId)
                .stream()
                .map(MemberDevice::getDevice)
                .toList();

        List<Long> deviceIds = devices.stream().map(Device::getId).toList();

        HomeResponse.CurrentStatus currentStatus = resolveCurrentStatus(devices);
        HomeResponse.TodaySummary todaySummary = resolveTodaySummary(memberId, deviceIds);
        List<HomeResponse.DevicePreview> devicePreviews = devices.stream()
                .map(HomeResponse.DevicePreview::from)
                .toList();
        List<NotificationResponse.NotificationItem> recentNotifications =
                notificationService.getRecentNotifications(memberId, RECENT_NOTIFICATION_LIMIT);
        List<HomeResponse.DeviceStatusPreview> deviceStatuses = devices.stream()
                .map(HomeResponse.DeviceStatusPreview::from)
                .toList();

        return HomeResponse.Home.of(currentStatus, todaySummary, devicePreviews, recentNotifications, deviceStatuses);
    }

    private HomeResponse.CurrentStatus resolveCurrentStatus(List<Device> devices) {
        if (devices.isEmpty()) {
            return HomeResponse.CurrentStatus.from(null);
        }
        // 등록된 기기 중 하나라도 위험 상태이면 가장 심각한 상태를 반환
        DangerState worstState = devices.stream()
                .map(d -> dangerStateService.getOngoingState(d.getDeviceSerialNumber()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .min(java.util.Comparator.comparing(s ->
                        naranhi.backend.domain.safety.entity.Severity.valueOf(s.severity()).ordinal()))
                .orElse(null);
        return HomeResponse.CurrentStatus.from(worstState);
    }

    private HomeResponse.TodaySummary resolveTodaySummary(Long memberId, List<Long> deviceIds) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        long todayNotificationCount = notificationRecipientRepository
                .countTodayByMemberId(memberId, startOfDay, endOfDay);

        long todayCryingCount = deviceIds.isEmpty() ? 0L :
                safetyEventRepository.countByDeviceIdsAndEventTypeAndDetectedAtBetween(
                        deviceIds, EventType.CRYING, startOfDay, endOfDay);

        return HomeResponse.TodaySummary.of(todayNotificationCount, todayCryingCount);
    }
}
