package naranhi.backend.s3;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class PresignedUrlService {

    private final S3Presigner s3Presigner;

    @Value("${spring.s3.bucket}")
    private String bucket;

    @Value("${spring.s3.region}")
    private String region;

    @Value("${spring.s3.presigned-url-expiry-minutes}")
    private long expiryMinutes;

    /**
     * Jetson 보드용 - S3에 파일을 직접 PUT 업로드할 presigned URL 발급
     *
     * @param deviceSerial 장치 시리얼 (S3 경로 구분용)
     * @param fileType     "snapshot" | "video"
     * @param extension    파일 확장자 (jpg, mp4 등)
     * @return PUT presigned URL과 업로드 완료 후 사용할 S3 object key
     */
    public UploadTarget generatePutUrl(String deviceSerial, String fileType, String extension) {
        String key = buildKey(deviceSerial, fileType, extension);

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(expiryMinutes))
                        .putObjectRequest(putRequest)
                        .build()
        );

        return new UploadTarget(key, presigned.url().toString());
    }

    /**
     * Android 앱용 - S3에서 파일을 다운로드할 presigned GET URL 발급
     *
     * @param key S3 object key (snapshotUrl / videoUrl에 저장된 값)
     */
    public String generateGetUrl(String key) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(expiryMinutes))
                        .getObjectRequest(getRequest)
                        .build()
        );

        return presigned.url().toString();
    }

    public String buildPublicUrl(String key) {
        if (key == null || key.isBlank()) return null;
        return "https://%s.s3.%s.amazonaws.com/%s".formatted(bucket, region, key);
    }

    private String buildKey(String deviceSerial, String fileType, String extension) {
        // 예: devices/JETSON-001/snapshot/uuid.jpg
        return "devices/%s/%s/%s.%s".formatted(deviceSerial, fileType, UUID.randomUUID(), extension);
    }

    public record UploadTarget(String key, String uploadUrl) {}
}
