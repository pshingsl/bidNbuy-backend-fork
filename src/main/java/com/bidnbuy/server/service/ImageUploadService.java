package com.bidnbuy.server.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadService {
    String upload(MultipartFile file, String path);
}
