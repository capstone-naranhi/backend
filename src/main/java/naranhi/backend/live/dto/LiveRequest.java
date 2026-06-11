package naranhi.backend.live.dto;

import jakarta.validation.constraints.NotNull;

public class LiveRequest {

    public record CreateSession(
            @NotNull
            Long deviceId
    ) {}
}
