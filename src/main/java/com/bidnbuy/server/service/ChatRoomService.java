package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.ChatRoomCreateRequestDto;
import com.bidnbuy.server.dto.ChatRoomDto;
import com.bidnbuy.server.dto.ChatRoomListDto;
import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.entity.ChatRoomEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.repository.ChatRoomRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final UserService userService;
    private final AuctionProductsService auctionProductsService;
    private final ChatMessageService chatMessageService;

    //판매자 상품글에서 채팅방 생성
    public ChatRoomDto findOrCreateChatRoom(ChatRoomCreateRequestDto requestDto){
        //상품 조회
        AuctionProductsEntity products = auctionProductsService.findById(requestDto.getAuctionId());
        UserEntity seller = products.getUser();
        UserEntity buyer = userService.findById(requestDto.getBuyerId());

        //기존 채팅방 조회
        Optional<ChatRoomEntity> existingRoom = chatRoomRepository.findByBuyerIdAndSellerIdAndAuctionIdAndDeletedAtIsNull(
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
                .chatroomId(chatRoom.getChatroomId())
                .buyerId(chatRoom.getBuyerId().getUserId())
                .sellerId(chatRoom.getSellerId().getUserId())
                .auctionId(chatRoom.getAuctionId().getAuctionId())
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }

    //채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<ChatRoomListDto> getChatRoomList (Long currentUserId){
        UserEntity currentUser = userService.findById(currentUserId);
        List<ChatRoomEntity> chatRooms = chatRoomRepository.findActiveRoomsByUserId(currentUser);

        return chatRooms.stream()
                .map(entity -> convertToChatRoomListDto(entity, currentUserId))
                .map(this::processAuctionImage)
                .collect(Collectors.toList());
    }

    private ChatRoomListDto  convertToChatRoomListDto(ChatRoomEntity chatRoom, Long currentUserId){
        Long buyerUserIdSafe = null;
        Long sellerUserIdSafe = null;
        try {
            buyerUserIdSafe = chatRoom.getBuyerId() != null ? chatRoom.getBuyerId().getUserId() : null;
        } catch (Exception e) {
            log.warn("buyer reference not available (possibly deleted): chatroomId={}", chatRoom.getChatroomId());
        }
        try {
            sellerUserIdSafe = chatRoom.getSellerId() != null ? chatRoom.getSellerId().getUserId() : null;
        } catch (Exception e) {
            log.warn("seller reference not available (possibly deleted): chatroomId={}", chatRoom.getChatroomId());
        }

        boolean isCurrentUserBuyer = Objects.equals(buyerUserIdSafe, currentUserId);
        UserEntity counterpartUser = isCurrentUserBuyer ? chatRoom.getSellerId():chatRoom.getBuyerId();
        AuctionProductsEntity auctionProducts = chatRoom.getAuctionId();

        Long unreadCount = chatMessageService.getUnreadMessageCount(chatRoom.getChatroomId(), currentUserId);

        Long counterpartIdSafe = 0L;
        String counterpartNicknameSafe = "탈퇴회원";
        String counterpartProfileImageUrlSafe = null;
        try {
            if (counterpartUser == null || counterpartUser.isDeleted()) {
                counterpartNicknameSafe = "탈퇴회원";
                counterpartProfileImageUrlSafe = null;
            }else{
                counterpartIdSafe = counterpartUser.getUserId();
                counterpartNicknameSafe = counterpartUser.getNickname();
                counterpartProfileImageUrlSafe = counterpartUser.getProfileImageUrl();
            }
        } catch (Exception e) {
            log.warn("counterpart user not available (possibly deleted): chatroomId={}", chatRoom.getChatroomId());
        }

        String auctionTitleSafe = null;
        try {
            auctionTitleSafe = auctionProducts != null ? auctionProducts.getTitle() : null;
        } catch (Exception e) {
            log.warn("auction reference not available (possibly deleted): chatroomId={}", chatRoom.getChatroomId());
        }

        return ChatRoomListDto.builder()
                .chatroomId(chatRoom.getChatroomId())
                .auctionId(auctionProducts != null ? auctionProducts.getAuctionId() : null)
                .counterpartId(counterpartIdSafe)
                .counterpartNickname(counterpartNicknameSafe)
                .counterpartProfileImageUrl(counterpartProfileImageUrlSafe)
                .auctionTitle(auctionTitleSafe)
                .auctionImageUrl(null)
                .lastMessagePreview(chatRoom.getLastMessagePreview())
                .lastMessageTime(chatRoom.getLastMessageTime())
                .unreadCount(unreadCount.intValue())
                .build();
    }


    private ChatRoomListDto processAuctionImage(ChatRoomListDto dto){
        Long auctionId = dto.getAuctionId();

        try {
            Optional<AuctionProductsEntity> optionalProduct = auctionProductsService.findByIdAnyway(auctionId);

            if (optionalProduct.isPresent()) {
                AuctionProductsEntity product = optionalProduct.get();

                dto.setAuctionImageUrl(product.getMainImageUrl());
                dto.setAuctionTitle(product.getTitle());

            } else {
                log.warn("경매 상품 정보 조회 중 상품을 찾을 수 없습니다. Auction ID: {}", auctionId);
                dto.setAuctionTitle("삭제된 상품");
                dto.setAuctionImageUrl(null);
            }

        } catch (Exception e) {
            log.error("Auction ID {} 처리 중 치명적인 예외 발생: {}", auctionId, e.getMessage(), e);
            dto.setAuctionTitle("오류 발생 상품");
            dto.setAuctionImageUrl(null);
        }
        return dto;
    }

    @Transactional
    public void deletedChatRoom(Long chatroomId, Long currentUserId){
        ChatRoomEntity chatRoom = chatRoomRepository.findById(chatroomId)
                .orElseThrow(()-> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));

        Long buyerId = chatRoom.getBuyerId() != null? chatRoom.getBuyerId().getUserId():null;
        Long sellerId = chatRoom.getSellerId() != null? chatRoom.getSellerId().getUserId():null;

        if(!currentUserId.equals(buyerId) && !currentUserId.equals(sellerId)){
            throw new AccessDeniedException("채팅방 삭제 권한이 없습니다.");
        }

        //논리삭제 처리
        chatRoom.setDeletedAt(LocalDateTime.now());
        chatRoomRepository.save(chatRoom);
    }
}
