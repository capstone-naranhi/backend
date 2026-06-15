package naranhi.backend.home.dto;

import naranhi.backend.domain.safety.dto.DangerState;

public enum ChildStatus {
    SAFE, DANGER, CAUTION, INFO;

    public static ChildStatus from(DangerState state) {
        if (state == null) return SAFE;
        return switch (state.severity()) {
            case "DANGER" -> DANGER;
            case "CAUTION" -> CAUTION;
            case "INFO" -> INFO;
            default -> SAFE;
        };
    }
}
