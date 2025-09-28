package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.AuctionProductsDTO;
import com.bidnbuy.server.dto.ResponseDto;
import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.service.AuctionProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/auctions")
public class AuctionProductsController {

    @Autowired
    private AuctionProductsService service;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody AuctionProductsDTO dto) {
        try {
            // 1. DTO -> Entity 변환
            //    (DTO 클래스에 정의된 toEntity() 메서드를 사용)
            AuctionProductsEntity entity = dto.toEntity(dto);

            // 2. Service 계층 호출 및 저장된 Entity 받기
            //    (Service는 저장된 단일 Entity를 반환한다고 가정)
            AuctionProductsEntity createdEntity = service.create(entity);

            // 3. Entity -> DTO 변환 (응답을 위해)
            //    (DTO 클래스에 정의된 생성자를 사용하여 단일 DTO로 변환)
            AuctionProductsDTO createdDto = new AuctionProductsDTO(createdEntity);

            // 4. 단일 DTO를 List로 감싸 ResponseDto 구성 (기존 ResponseDto 구조 유지)
            ResponseDto<AuctionProductsDTO> response = ResponseDto.<AuctionProductsDTO>builder()
                    .data(Collections.singletonList(createdDto))
                    .build();

            // 5. 성공 응답 반환
            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            // 6. 예외 처리 로직 (오류 응답)
            ResponseDto<Object> response = ResponseDto.builder()
                    .error(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }
}
