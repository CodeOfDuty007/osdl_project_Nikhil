package com.hotelos.models;

/**
 * 1. Object-Oriented Programming (OOP)
 * Inheritance: LuxuryRoom extends Room.
 * Implements Amenities interface.
 */
public class LuxuryRoom extends Room implements Amenities {
    private static final long serialVersionUID = 1L;

    public LuxuryRoom(Integer roomNumber, RoomType roomType) {
        // Can be DELUXE or SUITE
        super(roomNumber, roomType);
    }

    // Demonstrating Polymorphism via method overriding
    @Override
    public double calculateTariff(int daysStayed) {
        // Luxury tax of 10%
        double base = getRoomType().calculateBaseTariff(daysStayed);
        return base + (base * 0.10);
    }

    @Override
    public String provideWifi() {
        return "Premium High-Speed Wi-Fi (up to 100 Mbps).";
    }

    @Override
    public String provideBreakfast() {
        return "Gourmet buffet breakfast and room service included.";
    }

    @Override
    public String toString() {
        return "Luxury Room (" + getRoomType() + ") - No. " + getRoomNumber() + " | Price: $" + getRoomType().getBasePrice() + "/night";
    }
}
