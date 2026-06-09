package naranhi.backend.domain.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DeviceRequest {

    public record Register(
            @NotBlank
            String deviceSerial,

            @NotBlank
            @Size(max = 20)
            String deviceName,

            @NotBlank
            @Size(max = 100)
            String locationName
    ) {}
}
