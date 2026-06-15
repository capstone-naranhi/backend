package naranhi.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Value("${spring.s3.region}")
    private String region;

    /**
     * 액세스 키 없이 EC2 IAM 역할에서 자격증명을 자동으로 가져옵니다.
     * (DefaultCredentialsProvider → 환경변수 → 인스턴스 메타데이터 순서로 탐색)
     */
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .build();
    }
}
