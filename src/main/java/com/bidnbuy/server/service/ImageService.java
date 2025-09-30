package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.ImageDto;
import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.entity.ImageEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.repository.AuctionProductsRepository;
import com.bidnbuy.server.repository.ImageRepository;
import com.bidnbuy.server.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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
}