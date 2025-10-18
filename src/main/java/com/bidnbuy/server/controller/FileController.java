package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.ImageDto;
import com.bidnbuy.server.service.ImageService;
import com.bidnbuy.server.service.S3UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class FileController {

//    private final S3UploadService s3UploadService;
//    private final ImageService imageService;
//
//
//    @PostMapping("/upload")
//    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
//        try {
//            String imageUrl = s3UploadService.uploadFile(file, "post-images/");
//
//            return ResponseEntity.ok("업로드 성공. URL: " + imageUrl);
//        } catch (IOException e) {
//            return ResponseEntity.status(500).body("파일 업로드 실패: " + e.getMessage());
//        }
//    }
}