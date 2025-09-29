package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.ImageDTO;
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



@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private AuctionProductsRepository auctionProductsRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ImageEntity create(ImageDTO dto, Long userId) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다!"));

        ImageEntity image = ImageEntity.builder()
                .imageUrl(dto.getImageUrl())
                .user(user)
                .build();

        return imageRepository.save(image);
    }

//    @Transactional
//    public List<ImageEntity> getImagesByAuction(Long auctionId) {
//        return imageRepository.findAllByAuctionProductId(auctionId);
//    }
}