package com.bidnbuy.server.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

public class LocalImageUploadService implements ImageUploadService {

        @Value("${file.upload-dir}")
    private String uploadDir;

    private final String BASE_URL = "/images/";

    @Override
    public String upload(MultipartFile file, String path) {
        if (file.isEmpty()) {
            return null;
        }

        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            // 최종 경로 지정
            Path targetLocation = Paths.get(uploadDir + "/" + path).toAbsolutePath().normalize();
            Files.createDirectories(targetLocation); // 디렉토리가 없으면 생성

            Path targetFile = targetLocation.resolve(fileName);

            // 3. 파일 저장
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            // 4. DB에 저장될 영구적인 접근 URL 반환 (URL은 /images/ 경로로 매핑되어야 함)
            // 예: /images/auctions/3/uuid_name.jpg
            return BASE_URL + path + "/" + fileName;

        } catch (IOException ex) {
            throw new RuntimeException("이미지 파일 저장에 실패했습니다.", ex);
        }
    }
}

