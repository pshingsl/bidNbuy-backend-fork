package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.ChatRoomCreateRequestDto;
import com.bidnbuy.server.dto.ChatRoomDto;
import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.entity.ChatRoomEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final UserService userService;
    private final AuctionProductsService auctionProductsService;

    //판매자 상품글에서 채팅방 생성
    public ChatRoomDto findOrCreateChatRoom(ChatRoomCreateRequestDto requestDto){
        //상품 조회
        AuctionProductsEntity products = auctionProductsService.findById(requestDto.getAuctionId());
        UserEntity seller = products.getUser();

        UserEntity buyer = userService.findById(requestDto.getBuyerId());

        //기존 채팅방 조회
        Optional<ChatRoomEntity> existingRoom = chatRoomRepository.findByBuyerIdAndSellerIdAndAuctionId(
                buyer,
                seller,
                products
        );
        ChatRoomEntity chatRoom;

        if(existingRoom.isPresent()){
            chatRoom = existingRoom.get();
        }else{
            chatRoom = ChatRoomEntity.builder()
                    .buyerId(buyer)
                    .sellerId(seller)
                    .auctionId(products)
                    .build();
            chatRoom = chatRoomRepository.save(chatRoom);
        }
        return ChatRoomDto.builder()
                .chatroomId(chatRoom.getChatroomId())
                .buyerId(chatRoom.getBuyerId().getUserId())
                .sellerId(chatRoom.getSellerId().getUserId())
                .auctionId(chatRoom.getAuctionId().getAuctionId())
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }
}
