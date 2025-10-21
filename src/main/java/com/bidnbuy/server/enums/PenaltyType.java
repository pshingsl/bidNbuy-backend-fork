package com.bidnbuy.server.enums;

public enum PenaltyType {
    LEVEL_1(10),
    LEVEL_2(30),
    LEVEL_3(50);
    
    private final int points;
    
    PenaltyType(int points) {
        this.points = points;
    }
    
    public int getPoints() {
        return points;
    }
}