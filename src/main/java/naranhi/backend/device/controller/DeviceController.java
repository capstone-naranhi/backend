package naranhi.backend.device.controller;

import lombok.RequiredArgsConstructor;
import naranhi.backend.device.dto.DeviceResponse;
import naranhi.backend.device.service.DeviceService;
import naranhi.backend.global.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping("/{deviceId}")
    public ApiResponse<DeviceResponse.DeviceDetail> getDeviceDetail(
            @PathVariable Long deviceId,
            @RequestParam Long memberId
    ) {
        return ApiResponse.ok(deviceService.getDeviceDetail(memberId, deviceId));
    }

    @GetMapping
    public ApiResponse<DeviceResponse.DeviceList> getDevices(
            @RequestParam Long memberId
    ) {
        return ApiResponse.ok(deviceService.getDevices(memberId));
    }

    @GetMapping("/{deviceId}/live-status")
    public ApiResponse<DeviceResponse.LiveStreamStatus> getLiveStreamStatus(
            @PathVariable Long deviceId,
            @RequestParam Long memberId
    ) {
        return ApiResponse.ok(deviceService.getLiveStreamStatus(memberId, deviceId));
    }
}
