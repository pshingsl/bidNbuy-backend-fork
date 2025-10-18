package com.bidnbuy.server.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3UploadService {

//    private final S3Client s3Client;
//
//    @Value("${cloud.aws.s3.bucket}")
//    private String bucket;
//
//    @Value("${spring.cloud.aws.region.static}")
//    private String region;
//
//    public S3UploadService(S3Client s3Client) {
//        this.s3Client = s3Client;
//    }
//
//    public String uploadFile(MultipartFile multipartFile, String directory) throws IOException {
//
//        // 파일 이름 생성
//        String originalFileName = multipartFile.getOriginalFilename();
//        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
//        String s3FileName = directory + UUID.randomUUID().toString() + extension;
//
//        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
//                .bucket(bucket)
//                .key(s3FileName) // S3에 저장될 최종 경로와 파일 이름
//                .contentType(multipartFile.getContentType()) // 파일 타입 지정
//                .contentLength(multipartFile.getSize()) // 파일 크기 지정
//                .acl(ObjectCannedACL.PUBLIC_READ) //  누가 이 객체에 접근할 수 있는지 정의하는 AWS의 권한 설정
//                .build();
//
//        s3Client.putObject(putObjectRequest,
//                RequestBody.fromInputStream(multipartFile.getInputStream(),
//                        multipartFile.getSize()));
//
//        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, s3FileName);
//    }
//
//    public void deleteFile(String fileUrl) {
//        try {
//            // URL에서 S3 Key (파일명) 추출
//            String s3Key = fileUrl.substring(fileUrl.indexOf(".com/") + 5);
//
//            s3Client.deleteObject(builder -> builder
//                    .bucket(bucket)
//                    .key(s3Key));
//
//        } catch (Exception e) {
//            // 삭제 실패 처리 (파일이 이미 없거나 권한 문제 등)
//            System.err.println("S3 파일 삭제 실패: " + e.getMessage());
//        }
//    }
}