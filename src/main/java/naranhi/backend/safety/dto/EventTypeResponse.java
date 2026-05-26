package naranhi.backend.safety.dto;

import naranhi.backend.safety.entity.EventType;

public record EventTypeResponse(
        String code,
        String description
) {
    public static EventTypeResponse from(EventType eventType) {
        return new EventTypeResponse(eventType.name(), eventType.getDescription());
    }
}
