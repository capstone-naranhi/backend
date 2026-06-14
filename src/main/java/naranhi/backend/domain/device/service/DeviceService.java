package naranhi.backend.domain.device.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import naranhi.backend.domain.device.dto.DeviceRequest;
import naranhi.backend.domain.device.dto.DeviceResponse;
import naranhi.backend.domain.device.entity.Device;
import naranhi.backend.domain.device.entity.DeviceStatus;
import naranhi.backend.domain.device.entity.MemberDevice;
import naranhi.backend.domain.device.repository.DeviceRepository;
import naranhi.backend.domain.device.repository.MemberDeviceRepository;
import naranhi.backend.domain.device.repository.mongo.DeviceStatusChangeLogRepository;
import naranhi.backend.domain.member.entity.Member;
import naranhi.backend.domain.member.repository.MemberRepository;
import naranhi.backend.domain.safety.dto.DangerState;
import naranhi.backend.domain.safety.service.DangerStateService;
import naranhi.backend.global.exception.CustomException;
import naranhi.backend.global.exception.ErrorCode;
import naranhi.backend.log.document.DeviceStatusLog;
import naranhi.backend.mqtt.processor.HeartbeatProcessor;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final MemberDeviceRepository memberDeviceRepository;
    private final MemberRepository memberRepository;
    private final DeviceStatusChangeLogRepository deviceStatusChangeLogRepository;
    private final StringRedisTemplate redisTemplate;
    private final DangerStateService dangerStateService;

    public DeviceResponse.DeviceDetail getDeviceDetail(Long memberId, Long deviceId) {
        if (!memberDeviceRepository.existsByMemberIdAndDeviceId(memberId, deviceId)) {
            throw new CustomException(ErrorCode.DEVICE_ACCESS_DENIED);
        }

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new CustomException(ErrorCode.DEVICE_NOT_FOUND));

        List<DeviceStatusLog> logs = deviceStatusChangeLogRepository
                .findTop3ByDeviceId(deviceId, Sort.by(Sort.Direction.DESC, "changedAt"));

        return DeviceResponse.DeviceDetail.of(device, logs);
    }

    public DeviceResponse.DeviceList getDevices(Long memberId) {
        List<Device> devices = memberDeviceRepository.findAllWithDeviceByMemberId(memberId)
                .stream()
                .map(md -> md.getDevice())
                .toList();
        return DeviceResponse.DeviceList.from(devices);
    }

    public DeviceResponse.LiveStreamStatus getLiveStreamStatus(Long memberId, Long deviceId) {
        if (!memberDeviceRepository.existsByMemberIdAndDeviceId(memberId, deviceId)) {
            throw new CustomException(ErrorCode.DEVICE_ACCESS_DENIED);
        }

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new CustomException(ErrorCode.DEVICE_NOT_FOUND));

        DangerState dangerState = dangerStateService
                .getCurrent(device.getDeviceSerialNumber())
                .orElse(null);

        return DeviceResponse.LiveStreamStatus.of(device, dangerState);
    }

    @Transactional
    public DeviceResponse.RegisteredDevice registerDevice(Long memberId, DeviceRequest.Register request) {
        String pendingKey = HeartbeatProcessor.PENDING_KEY_PREFIX + request.deviceSerial();

        if (!Boolean.TRUE.equals(redisTemplate.hasKey(pendingKey))) {
            throw new CustomException(ErrorCode.DEVICE_PENDING_NOT_FOUND);
        }

        if (deviceRepository.existsByDeviceSerialNumber(request.deviceSerial())) {
            throw new CustomException(ErrorCode.DEVICE_ALREADY_REGISTERED);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Device device = Device.builder()
                .deviceSerialNumber(request.deviceSerial())
                .deviceName(request.deviceName())
                .locationName(request.locationName())
                .boardStatus(DeviceStatus.ONLINE)
                .cameraStatus(DeviceStatus.OFFLINE)
                .micStatus(DeviceStatus.OFFLINE)
                .mqttClientId(request.deviceSerial())
                .lastHeartbeatAt(LocalDateTime.now())
                .build();

        deviceRepository.save(device);

        MemberDevice memberDevice = MemberDevice.builder()
                .member(member)
                .device(device)
                .build();

        memberDeviceRepository.save(memberDevice);

        redisTemplate.delete(pendingKey);

        return DeviceResponse.RegisteredDevice.from(device);
    }
}
