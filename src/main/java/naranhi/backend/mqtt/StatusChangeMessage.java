package naranhi.backend.mqtt;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StatusChangeMessage(
        String serial,
        @JsonProperty("component_type") String componentType,  // CAMERA, MIC, BOARD
        @JsonProperty("prev_status") String prevStatus,        // ONLINE, OFFLINE
        @JsonProperty("curr_status") String currStatus,
        String reason
) {
}