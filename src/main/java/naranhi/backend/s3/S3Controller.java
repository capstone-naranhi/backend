package naranhi.backend.s3;

import lombok.RequiredArgsConstructor;
import naranhi.backend.global.response.ApiResponse;
import naranhi.backend.s3.PresignedUrlService.UploadTarget;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final PresignedUrlService presignedUrlService;

    /**
     * Jetson 보드 → S3 직접 업로드용 PUT presigned URL 발급
     *
     * GET /api/v1/s3/presigned/upload
     *   ?deviceSerial=JETSON-001
     *   &fileType=snapshot          (snapshot | video)
     *   &extension=jpg              (jpg | mp4 등)
     *
     * 응답: { key: "devices/JETSON-001/snapshot/uuid.jpg", uploadUrl: "https://..." }
     */
    @GetMapping("/presigned/upload")
    public ApiResponse<UploadTarget> getUploadUrl(
            @RequestParam String deviceSerial,
            @RequestParam String fileType,
            @RequestParam String extension
    ) {
        return ApiResponse.ok(presignedUrlService.generatePutUrl(deviceSerial, fileType, extension));
    }

    /**
     * Android 앱 → S3 파일 조회용 GET presigned URL 발급
     *
     * GET /api/v1/s3/presigned/download
     *   ?key=devices/JETSON-001/snapshot/uuid.jpg
     *
     * 응답: { url: "https://..." }
     */
    @GetMapping("/presigned/download")
    public ApiResponse<DownloadUrl> getDownloadUrl(
            @RequestParam String key
    ) {
        return ApiResponse.ok(new DownloadUrl(presignedUrlService.generateGetUrl(key)));
    }

    public record DownloadUrl(String url) {}
}
