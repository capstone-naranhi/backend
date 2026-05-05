package naranhi.backend.domain.device.service;

import lombok.RequiredArgsConstructor;
import naranhi.backend.domain.device.dto.DeviceResponse;
import naranhi.backend.domain.device.entity.Device;
import naranhi.backend.domain.device.repository.DeviceRepository;
import naranhi.backend.domain.device.repository.MemberDeviceRepository;
import naranhi.backend.domain.safety.entity.SafetyEvent;
import naranhi.backend.domain.safety.repository.SafetyEventRepository;
import naranhi.backend.global.exception.CustomException;
import naranhi.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final MemberDeviceRepository memberDeviceRepository;
    private final SafetyEventRepository safetyEventRepository;

    public DeviceResponse.LiveStreamStatus getLiveStreamStatus(Long memberId, Long deviceId) {
        if (!memberDeviceRepository.existsByMemberIdAndDeviceId(memberId, deviceId)) {
            throw new CustomException(ErrorCode.DEVICE_ACCESS_DENIED);
        }

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new CustomException(ErrorCode.DEVICE_NOT_FOUND));

        SafetyEvent lastSafetyEvent = safetyEventRepository
                .findTopByDeviceIdOrderByDetectedAtDesc(deviceId)
                .orElse(null);

        return DeviceResponse.LiveStreamStatus.of(device, lastSafetyEvent);
    }
}
