package com.bidnbuy.server.service;

import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.entity.ImageEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.enums.ImageType;
import com.bidnbuy.server.repository.AuctionProductsRepository;
import com.bidnbuy.server.repository.ImageRepository;
import com.bidnbuy.server.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;


@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private AuctionProductsRepository auctionProductsRepository;

    @Autowired
    private UserRepository userRepository;

    // application.yml에 정의된 기본 업로드 경로 (예: C:/temp/bidnbuy_uploads)
    @Value("${file.upload-dir}")
    private String UPLOAD_BASE_DIR;

    // 경매 상품 이미지용 웹 접근 경로 (WebConfig와 일치해야 함)
    private final String AUCTION_WEB_ACCESS_PREFIX = "/images/auction-products/";

    // 유저 이미지용 웹 접근 경로 (WebConfig와 일치해야 함)
    private final String PROFILE_WEB_ACCESS_PREFIX = "/images/user-profiles/";

    /**
     * 경매 상품 이미지를 로컬에 저장하고 영구 URL을 반환합니다.
     * 이 메서드가 반환하는 URL은 AuctionProductsService에서 ImageEntity에 저장됩니다.
     */
    public String uploadAuctionImage(Long auctionId, MultipartFile imageFile) {

        if (imageFile.isEmpty()) {
            throw new IllegalArgumentException("업로드할 이미지가 없습니다.");
        }

        // 1. 경매 상품 존재 확인
        auctionProductsRepository.findById(auctionId)
                .orElseThrow(() -> new EntityNotFoundException("해당 경매 아이디가 존재하지 않습니다!"));

        // 2. 파일 저장 경로 설정 (UPLOAD_BASE_DIR/auction-products/경매ID/)
        String subPath = "auction-products/" + auctionId.toString();
        Path uploadPath = Paths.get(UPLOAD_BASE_DIR, subPath).toAbsolutePath().normalize();
        String newWebAccessUrl = "";

        try {
            // 디렉토리가 없으면 생성
            Files.createDirectories(uploadPath);

            // 3. 파일 이름 생성 및 저장
            String originalFilename = imageFile.getOriginalFilename();
            String fileExtension = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String newFileName = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(newFileName);

            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 4. DB에 저장될 영구적인 웹 접근 URL 생성
            // 형식: /images/auction-products/경매ID/파일명
            newWebAccessUrl = AUCTION_WEB_ACCESS_PREFIX + auctionId + "/" + newFileName;
        } catch (IOException e) {
            throw new RuntimeException("경매 이미지 로컬 파일 업로드에 실패했습니다.", e);
        }

        return newWebAccessUrl;
    }


    /**
     * 사용자 프로필 이미지를 로컬에 저장하고 DB를 갱신합니다.
     */
    @Transactional
    public String updateProfileImage(Long userId, MultipartFile imageFile) {
        if (imageFile.isEmpty()) {
            throw new IllegalArgumentException("업로드할 이미지가 없습니다.");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User Not Found with ID: " + userId));

        // 1. 로컬 파일 저장 로직 수행
        String newWebAccessUrl = "";

        // 프로필 파일 저장 경로 설정 (UPLOAD_BASE_DIR/user-profiles/)
        String subPath = "user-profiles";
        Path uploadPath = Paths.get(UPLOAD_BASE_DIR, subPath).toAbsolutePath().normalize();

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = imageFile.getOriginalFilename();
            String fileExtension = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String newFileName = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(newFileName);

            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING); // 파일 저장

            // DB에 저장할 URL 생성
            newWebAccessUrl = PROFILE_WEB_ACCESS_PREFIX + newFileName;

        } catch (IOException e) {
            throw new RuntimeException("프로필 이미지 로컬 파일 업로드에 실패했습니다.", e);
        }

        // 2. 기존 프로필 이미지 엔티티 조회 및 갱신/생성
        Optional<ImageEntity> existingImage = imageRepository.findByUser_UserId(userId);

        if (existingImage.isPresent()) {
            ImageEntity image = existingImage.get();
            // 기존 파일 삭제 (선택적)
            deleteLocalFile(image.getImageUrl());

            image.setImageUrl(newWebAccessUrl); // URL 갱신
            imageRepository.save(image);
        } else {
            // 새 엔티티 생성
            ImageEntity newImage = ImageEntity.builder()
                    .user(user)
                    .auctionProduct(null)
                    .imageUrl(newWebAccessUrl)
                    .imageType(ImageType.USER)
                    .build();
            imageRepository.save(newImage);
        }

        // 3. UserEntity에도 URL을 갱신
        user.setProfileImageUrl(newWebAccessUrl);
        userRepository.save(user);

        return newWebAccessUrl;
    }

    /**
     * 로컬 디스크에서 파일을 삭제하는 헬퍼 메서드 (경매/프로필 경로 모두 처리)
     */
    private void deleteLocalFile(String fileUrl) {
        if (fileUrl == null) return;

        String pathRelativeToBaseDir = null;

        // 1. 경매 상품 URL인지 확인
        if (fileUrl.startsWith(AUCTION_WEB_ACCESS_PREFIX)) {
            // /images/auction-products/ -> auction-products/3/uuid.jpg
            String relativeUrl = fileUrl.substring(AUCTION_WEB_ACCESS_PREFIX.length());
            pathRelativeToBaseDir = "auction-products/" + relativeUrl;
        }
        // 2. 프로필 URL인지 확인
        else if (fileUrl.startsWith(PROFILE_WEB_ACCESS_PREFIX)) {
            // /images/user-profiles/ -> user-profiles/uuid.jpg
            String relativeUrl = fileUrl.substring(PROFILE_WEB_ACCESS_PREFIX.length());
            pathRelativeToBaseDir = "user-profiles/" + relativeUrl;
        }
        else {
            return; // 처리할 수 없는 URL 형식
        }

        // UPLOAD_BASE_DIR과 상대 경로를 합쳐 실제 파일 경로 생성
        Path filePath = Paths.get(UPLOAD_BASE_DIR).resolve(pathRelativeToBaseDir);

        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            // 실제 서비스에서는 로깅 시스템을 사용해야 합니다.
            System.err.println("로컬 파일 삭제 실패: " + filePath + ", 오류: " + e.getMessage());
        }
    }
}