package naranhi.backend.domain.device.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import naranhi.backend.auth.LoginUser;
import naranhi.backend.auth.SessionUser;
import naranhi.backend.domain.device.dto.DeviceRequest;
import naranhi.backend.domain.device.dto.DeviceResponse;
import naranhi.backend.domain.device.service.DeviceService;
import naranhi.backend.global.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping("/register")
    public ApiResponse<DeviceResponse.RegisteredDevice> registerDevice(
            @LoginUser SessionUser loginUser,
            @Valid @RequestBody DeviceRequest.Register request
    ) {
        return ApiResponse.ok(deviceService.registerDevice(loginUser.getId(), request));
    }

    @GetMapping("/{deviceId}")
    public ApiResponse<DeviceResponse.DeviceDetail> getDeviceDetail(
            @LoginUser SessionUser loginUser,
            @PathVariable Long deviceId
    ) {
        return ApiResponse.ok(deviceService.getDeviceDetail(loginUser.getId(), deviceId));
    }

    @GetMapping
    public ApiResponse<DeviceResponse.DeviceList> getDevices(
            @LoginUser SessionUser loginUser
    ) {
        return ApiResponse.ok(deviceService.getDevices(loginUser.getId()));
    }

    @GetMapping("/{deviceId}/live-status")
    public ApiResponse<DeviceResponse.LiveStreamStatus> getLiveStreamStatus(
            @LoginUser SessionUser loginUser,
            @PathVariable Long deviceId
    ) {
        return ApiResponse.ok(deviceService.getLiveStreamStatus(loginUser.getId(), deviceId));
    }
}
