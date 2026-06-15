package naranhi.backend.home.dto;

import java.time.LocalDateTime;
import java.util.List;
import naranhi.backend.domain.device.entity.Device;
import naranhi.backend.domain.device.entity.DeviceStatus;
import naranhi.backend.domain.notification.dto.NotificationResponse;
import naranhi.backend.domain.safety.dto.DangerState;

public class HomeResponse {

    public record Home(
            CurrentStatus currentStatus,
            TodaySummary todaySummary,
            List<DevicePreview> devices,
            List<NotificationResponse.NotificationItem> recentNotifications,
            List<DeviceStatusPreview> deviceStatuses
    ) {
        public static Home of(
                CurrentStatus currentStatus,
                TodaySummary todaySummary,
                List<DevicePreview> devices,
                List<NotificationResponse.NotificationItem> recentNotifications,
                List<DeviceStatusPreview> deviceStatuses
        ) {
            return new Home(currentStatus, todaySummary, devices, recentNotifications, deviceStatuses);
        }
    }

    public record CurrentStatus(
            LocalDateTime evaluatedAt,
            ChildStatus childStatus
    ) {
        public static CurrentStatus from(DangerState ongoingState) {
            return new CurrentStatus(LocalDateTime.now(), ChildStatus.from(ongoingState));
        }
    }

    public record TodaySummary(
            long todayNotificationCount,
            long todayCryingCount
    ) {
        public static TodaySummary of(long todayNotificationCount, long todayCryingCount) {
            return new TodaySummary(todayNotificationCount, todayCryingCount);
        }
    }

    public record DevicePreview(
            Long deviceId,
            String deviceName
    ) {
        public static DevicePreview from(Device device) {
            return new DevicePreview(device.getId(), device.getDeviceName());
        }
    }

    public record DeviceStatusPreview(
            Long deviceId,
            String deviceName,
            DeviceStatus boardStatus,
            DeviceStatus cameraStatus,
            DeviceStatus micStatus
    ) {
        public static DeviceStatusPreview from(Device device) {
            return new DeviceStatusPreview(
                    device.getId(),
                    device.getDeviceName(),
                    device.getBoardStatus(),
                    device.getCameraStatus(),
                    device.getMicStatus()
            );
        }
    }
}
