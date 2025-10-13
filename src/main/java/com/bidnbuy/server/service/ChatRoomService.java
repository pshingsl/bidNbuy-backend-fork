package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.ChatRoomCreateRequestDto;
import com.bidnbuy.server.dto.ChatRoomDto;
import com.bidnbuy.server.dto.ChatRoomListDto;
import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.entity.ChatRoomEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.repository.ChatRoomRepository;
import com.sun.tools.jconsole.JConsoleContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
                    .lastMessagePreview(null)
                    .lastMessageTime(null)
                    .unreadCount(0)
                    .build();
            chatRoom = chatRoomRepository.save(chatRoom);
        }
        return ChatRoomDto.builder()
                .chatroomId(String.valueOf(chatRoom.getChatroomId()))
                .buyerId(String.valueOf(chatRoom.getBuyerId().getUserId()))
                .sellerId(String.valueOf(chatRoom.getSellerId().getUserId()))
                .auctionId(String.valueOf(chatRoom.getAuctionId().getAuctionId()))
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }

    //채팅방 목록 조회
    public List<ChatRoomListDto> getChatRoomList (Long currentUserId){
        UserEntity currentUser = userService.findById(currentUserId);
        List<ChatRoomEntity> chatRooms = chatRoomRepository.findByBuyerIdOrSellerIdOrderByLastMessageTimeDesc(currentUser, currentUser);

        return chatRooms.stream()
                .map(entity -> convertToChatRoomListDto(entity, currentUserId))
                .map(this::processAuctionImage)
                .collect(Collectors.toList());
    }

    private ChatRoomListDto  convertToChatRoomListDto(ChatRoomEntity chatRoom, Long currentUserId){
        boolean isCurrentUserBuyer = Objects.equals(chatRoom.getBuyerId().getUserId(), currentUserId);

        UserEntity counterpartUser = isCurrentUserBuyer ? chatRoom.getSellerId():chatRoom.getBuyerId();
        AuctionProductsEntity auctionProducts = chatRoom.getAuctionId();

        return ChatRoomListDto.builder()
                .chatroomId(String.valueOf(chatRoom.getChatroomId()))
                .auctionId(String.valueOf(auctionProducts.getAuctionId()))
                .counterpartId(String.valueOf(counterpartUser.getUserId()))
                .counterpartNickname(counterpartUser.getNickname())
                .counterpartProfileImageUrl(counterpartUser.getProfileImageUrl())
                .auctionTitle(auctionProducts.getTitle())
                .auctionImageUrl(null)
                .lastMessagePreview(chatRoom.getLastMessagePreview())
                .lastMessageTime(chatRoom.getLastMessageTime())
                .unreadCount(chatRoom.getUnreadCount())
                .build();
    }

    private ChatRoomListDto processAuctionImage(ChatRoomListDto dto){
        try{
            Long auctionId = Long.parseLong(dto.getAuctionId());
            AuctionProductsEntity products = auctionProductsService.findById(auctionId);
            if(products.getImages() !=null && !products.getImages().isEmpty()){
                dto.setAuctionImageUrl(products.getImages().get(0).getImageUrl());
            }
        }catch (Exception e){

        }
        return dto;
    }
}
