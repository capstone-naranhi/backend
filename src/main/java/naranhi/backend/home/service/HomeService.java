package naranhi.backend.home.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import naranhi.backend.domain.device.entity.Device;
import naranhi.backend.domain.device.entity.MemberDevice;
import naranhi.backend.domain.device.repository.MemberDeviceRepository;
import naranhi.backend.home.dto.HomeResponse;
import naranhi.backend.domain.notification.dto.NotificationResponse;
import naranhi.backend.domain.notification.repository.NotificationRecipientRepository;
import naranhi.backend.domain.notification.service.NotificationService;
import naranhi.backend.domain.safety.entity.EventType;
import naranhi.backend.domain.safety.entity.SafetyEvent;
import naranhi.backend.domain.safety.repository.SafetyEventRepository;
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

    public HomeResponse.Home getHome(Long memberId) {
        List<Device> devices = memberDeviceRepository.findAllWithDeviceByMemberId(memberId)
                .stream()
                .map(MemberDevice::getDevice)
                .toList();

        List<Long> deviceIds = devices.stream().map(Device::getId).toList();

        HomeResponse.CurrentStatus currentStatus = resolveCurrentStatus(deviceIds);
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

    private HomeResponse.CurrentStatus resolveCurrentStatus(List<Long> deviceIds) {
        if (deviceIds.isEmpty()) {
            return HomeResponse.CurrentStatus.from(null);
        }
        SafetyEvent lastEvent = safetyEventRepository
                .findTopByDeviceIdInOrderByDetectedAtDesc(deviceIds)
                .orElse(null);
        return HomeResponse.CurrentStatus.from(lastEvent);
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
