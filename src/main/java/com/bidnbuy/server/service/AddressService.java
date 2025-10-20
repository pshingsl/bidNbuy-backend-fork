package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.AddressRequestDto;
import com.bidnbuy.server.dto.AddressResponseDto;
import com.bidnbuy.server.dto.AddressUpdateDto;
import com.bidnbuy.server.entity.AddressEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.repository.AddressRepository;
import com.bidnbuy.server.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    // 모든 주소 조회
    @Transactional(readOnly = true)
    public List<AddressResponseDto> getAllAddresses(Long userId) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));


        return user.getAddress().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    // 주소 등록
    @Transactional
    public AddressResponseDto createAddress(Long userId, AddressRequestDto dto) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));


        AddressEntity address = AddressEntity.builder()
                .recipientName(dto.getName())
                .phoneNumber(dto.getPhoneNumber())
                .zonecode(dto.getZonecode())
                .address(dto.getAddress())
                .detailAddress(dto.getDetailAddress())
                .user(user) // 관계 설정
                .build();

        AddressEntity save = addressRepository.save(address);

        return toResponseDto(save);

    }

    // 주소 수정
    @Transactional
    public AddressResponseDto updateAddress(Long userId, Long addressId,  AddressUpdateDto dto) {

        AddressEntity address = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("수정할 주소 정보가 없습니다. Address ID: " + addressId));

        if (!address.getUser().getUserId().equals(userId)) {
            throw new EntityNotFoundException("접근 권한이 없거나 해당 주소 정보를 찾을 수 없습니다.");
        }

        if(dto.getName() != null){
            address.setRecipientName(dto.getName());
        }

        if(dto.getPhoneNumber() != null){
            address.setPhoneNumber(dto.getPhoneNumber());
        }

        if(dto.getZonecode() != null){
            address.setZonecode(dto.getZonecode());
        }

        if(dto.getAddress() != null){
            address.setAddress(dto.getAddress());
        }

        if(dto.getDetailAddress() != null){
            address.setDetailAddress(dto.getDetailAddress());
        }

        AddressEntity update = addressRepository.save(address);

        return toResponseDto(update);
    }

    // 주소 삭제
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {

        AddressEntity address = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 주소 정보를 찾을 수 없습니다. Address ID: " + addressId));

        if (!address.getUser().getUserId().equals(userId)) {
            throw new EntityNotFoundException("접근 권한이 없거나 해당 주소 정보를 찾을 수 없습니다.");
        }

        // AddressEntity를 직접 삭제
        addressRepository.delete(address);
    }


    private AddressResponseDto toResponseDto(AddressEntity entity) {
        return AddressResponseDto.builder()
                .addressId(entity.getAddressId())
                .name(entity.getRecipientName())
                .phoneNumber(entity.getPhoneNumber())
                .zonecode(entity.getZonecode())
                .address(entity.getAddress())
                .detailAddress(entity.getDetailAddress())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
