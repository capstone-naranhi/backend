package naranhi.backend.live.dto;

public class LiveResponse {

    public record Session(
            String sessionId,
            String deviceSerial
    ) {}
}
