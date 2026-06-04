package naranhi.backend.mqtt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Jetson → 서버 토픽: devices/{device_serial}/status/change
 * <p>
 * component_type: BOARD | CAMERA | MIC  (ComponentType enum) before_status:  ONLINE | OFFLINE | ERROR  (ComponentStatus
 * enum) current_status: ONLINE | OFFLINE | ERROR  (ComponentStatus enum)
 */
public record StatusChangeMessage(
        @JsonProperty("device_serial_number")
        String deviceSerialNumber,

        @JsonProperty("component_type")
        String componentType,   // ComponentType enum name

        @JsonProperty("before_status")
        String beforeStatus,    // ComponentStatus enum name

        @JsonProperty("current_status")
        String currentStatus,   // ComponentStatus enum name

        String description
) {
}
