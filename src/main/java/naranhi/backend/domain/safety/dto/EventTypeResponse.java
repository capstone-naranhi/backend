package naranhi.backend.domain.safety.dto;

import naranhi.backend.domain.safety.entity.EventType;

public record EventTypeResponse(
        String code,
        String description
) {
    public static EventTypeResponse from(EventType eventType) {
        return new EventTypeResponse(eventType.name(), eventType.getDescription());
    }
}
