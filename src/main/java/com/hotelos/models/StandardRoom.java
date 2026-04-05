package com.hotelos.models;

/**
 * 1. Object-Oriented Programming (OOP)
 * Inheritance: StandardRoom extends Room.
 * Implements Amenities interface.
 */
public class StandardRoom extends Room implements Amenities {
    private static final long serialVersionUID = 1L;

    public StandardRoom(Integer roomNumber) {
        super(roomNumber, RoomType.STANDARD);
    }

    // Demonstrating Polymorphism via method overriding
    @Override
    public double calculateTariff(int daysStayed) {
        // Simple base calculation
        return getRoomType().calculateBaseTariff(daysStayed);
    }

    @Override
    public String provideWifi() {
        return "Standard Wi-Fi access (up to 5 Mbps).";
    }

    @Override
    public String provideBreakfast() {
        return "Continental breakfast included.";
    }

    @Override
    public String toString() {
        return "Standard Room - No. " + getRoomNumber() + " | Price: $" + getRoomType().getBasePrice() + "/night";
    }
}
