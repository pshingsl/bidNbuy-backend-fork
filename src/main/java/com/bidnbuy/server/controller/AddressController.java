package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.AddressRequestDto;
import com.bidnbuy.server.dto.AddressResponseDto;
import com.bidnbuy.server.dto.AddressUpdateDto;
import com.bidnbuy.server.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    public ResponseEntity<?> getAllAddresses(@AuthenticationPrincipal Long userId) {

        List<AddressResponseDto> response = addressService.getAllAddresses(userId);

        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<?> createAddress(@AuthenticationPrincipal Long userId,
                                           @RequestBody @Valid AddressRequestDto dto) {

        AddressResponseDto response = addressService.createAddress(userId, dto);

        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<?> updateAddress(@AuthenticationPrincipal Long userId,
                                           @PathVariable Long addressId,
                                           @RequestBody AddressUpdateDto dto) {

        AddressResponseDto response = addressService.updateAddress(userId, addressId, dto);

        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<?> deleteAddress(@AuthenticationPrincipal Long userId, @PathVariable Long addressId) {

       addressService.deleteAddress(userId, addressId);

        return ResponseEntity.noContent().build();
    }
}
