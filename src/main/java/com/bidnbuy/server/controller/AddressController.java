package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.AddressRequestDto;
import com.bidnbuy.server.dto.AddressResponseDto;
import com.bidnbuy.server.dto.AddressUpdateDto;
import com.bidnbuy.server.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "주소 API", description = "주소 관련 기능 제공")
@RestController
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Operation(summary = "주소 조회", description = "주소 조회시 사용되는 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AddressResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public ResponseEntity<?> getAllAddresses(@AuthenticationPrincipal Long userId) {

        List<AddressResponseDto> response = addressService.getAllAddresses(userId);

        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "주소 상세 조회", description = "주소 상세 조회시 사용되는 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AddressResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/{addressId}")
    public ResponseEntity<?> getAddressById(@AuthenticationPrincipal Long userId,  @Parameter(description = "조회할 주소 ID", required = true) @PathVariable Long addressId) {

        AddressResponseDto response = addressService.getAddressById(userId, addressId);

        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "주소 생성", description = "주소 생성 사용되는 API")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = AddressResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping
    public ResponseEntity<?> createAddress(@AuthenticationPrincipal Long userId,
                                           @RequestBody @Valid AddressRequestDto dto) {

        AddressResponseDto response = addressService.createAddress(userId, dto);

        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "주소 수정", description = "주소 수정 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = AddressResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "입력 값 검증 실패 (유효하지 않은 데이터)",
                    content = @Content(schema = @Schema(example = "필수 필드가 누락되었거나 형식이 유효하지 않습니다."))),
            @ApiResponse(responseCode = "403", description = "본인 주소가 아니어서 권한 없음"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 주소를 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PutMapping("/{addressId}")
    public ResponseEntity<?> updateAddress(@AuthenticationPrincipal Long userId,
                                           @PathVariable Long addressId,
                                           @Parameter(description = "조회할 주소 ID", required = true) @RequestBody AddressUpdateDto dto) {

        AddressResponseDto response = addressService.updateAddress(userId, addressId, dto);

        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "주소 삭제", description = "주소 삭제 API")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공 (No Content)"),
            @ApiResponse(responseCode = "403", description = "본인 주소가 아니어서 권한 없음"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 주소를 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @DeleteMapping("/{addressId}")
    public ResponseEntity<?> deleteAddress(@AuthenticationPrincipal Long userId, @PathVariable Long addressId) {

        addressService.deleteAddress(userId, addressId);

        return ResponseEntity.noContent().build();
    }
}
