package naranhi.backend.domain.home.dto;

import naranhi.backend.domain.safety.entity.SafetyEvent;

public enum ChildStatus {
    SAFE, DANGER, CAUTION, INFO;

    public static ChildStatus from(SafetyEvent lastEvent) {
        if (lastEvent == null || lastEvent.getDurationSecond() == null || lastEvent.getDurationSecond() != 0) {
            return SAFE;
        }
        return switch (lastEvent.getSeverity()) {
            case DANGER -> DANGER;
            case CAUTION -> CAUTION;
            case INFO -> INFO;
        };
    }
}
