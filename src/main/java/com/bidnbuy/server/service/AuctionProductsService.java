package com.bidnbuy.server.service;

import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.repository.AuctionProductsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuctionProductsService {
    @Autowired
    private AuctionProductsRepository repository;

    // create
    public AuctionProductsEntity create(AuctionProductsEntity entity) {
       return repository.save(entity);
    }
}
