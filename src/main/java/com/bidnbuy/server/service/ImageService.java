package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.ImageDto;
import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.entity.ImageEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.enums.ImageType;
import com.bidnbuy.server.repository.AuctionProductsRepository;
import com.bidnbuy.server.repository.ImageRepository;
import com.bidnbuy.server.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private AuctionProductsRepository auctionProductsRepository;

    @Autowired
    private UserRepository userRepository;

    public List<String> uploadAuctionImages(Long auctionId, List<ImageDto> images) {
        AuctionProductsEntity auctionProduct = auctionProductsRepository.findById(auctionId)
                .orElseThrow(() -> new EntityNotFoundException("해당 경매 아이디가 존재하지 않습니다!"));

        List<String> imageUrls = images.stream()
                .map(dto -> {

                    // ImageEntity를 생성하고 AuctionProductsEntity와 연결
                    ImageEntity image = ImageEntity.builder()
                            .auctionProduct(auctionProduct)
                            .user(null)
                            .imageUrl(dto.getImageUrl())
                            .imageType(dto.getImageType())
                            .build();

                    imageRepository.save(image);

                    return dto.getImageUrl();
                })
                .collect(Collectors.toList());
        // 저장된 URL 리스트를 반환합니다
        return imageUrls;
    }

    // TODO 아직 aws 사용하지않아 여기에다 하드 코딩함
    private final String UPLOAD_BASE_DIR = "C:/temp/bidnbuy_uploads/user-profiles/";
    private final String WEB_ACCESS_PREFIX = "/images/user-profiles/";

    @Transactional
    public String updateProfileImage(Long userId, MultipartFile imageFile) {
        if (imageFile.isEmpty()) {
            throw new IllegalArgumentException("업로드할 이미지가 없습니다.");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User Not Found with ID: " + userId));

        // 1. 로컬 파일 저장 로직 수행
        String newWebAccessUrl = "";
        try {
            // 디렉토리 생성 및 파일 저장 (이전에 논의된 로컬 저장 로직)
            Path uploadPath = Paths.get(UPLOAD_BASE_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = imageFile.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(newFileName);

            Files.copy(imageFile.getInputStream(), filePath); // 파일 저장

            newWebAccessUrl = WEB_ACCESS_PREFIX + newFileName; // DB에 저장할 URL

        } catch (IOException e) {
            throw new RuntimeException("로컬 파일 업로드에 실패했습니다.", e);
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
                    .auctionProduct(null) // 유저 프로필이므로 NULL
                    .imageUrl(newWebAccessUrl)
                    .imageType(ImageType.USER) // NOT NULL 제약 때문에 값을 넣어줍니다.
                    .build();
            imageRepository.save(newImage);
        }

        // 3. UserEntity에도 URL을 갱신 (선택적이지만, 효율성을 위해 권장)
        user.setProfileImageUrl(newWebAccessUrl);
        userRepository.save(user);

        return newWebAccessUrl;
    }

    /**
     * 로컬 디스크에서 파일을 삭제하는 헬퍼 메서드
     */
    private void deleteLocalFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(WEB_ACCESS_PREFIX)) return;

        // 클라이언트 접근 URL에서 파일 이름만 추출
        String fileName = fileUrl.substring(WEB_ACCESS_PREFIX.length());
        Path filePath = Paths.get(UPLOAD_BASE_DIR).resolve(fileName);

        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            System.err.println("로컬 파일 삭제 실패: " + filePath + ", 오류: " + e.getMessage());
        }
    }
}