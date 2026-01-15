package com.bidnbuy.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
//aws 키발급 받으면 WebMvcConfig, WebConfig 삭제 -> 추후 S3 연결 종료시 로컬에서 사용
public class S3Config {

    @Value("${SPRING_AWS_ACCESS_KEY_ID}")
    private String accessKey;

    @Value("${SPRING_AWS_SECRET_ACCESS_KEY}")
    private String secretKey;

    @Value("${SPRING_AWS_REGION}")
    private String region;

    @Bean
    public S3Client s3Client() {
        // 자격 증명
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        // 2. S3Client를 빌더 패턴으로 생성 및 구성
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}