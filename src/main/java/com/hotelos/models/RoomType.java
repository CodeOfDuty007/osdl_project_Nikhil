package com.hotelos.models;

/**
 * 2. Wrapper Classes & Enumeration
 * Enum named RoomType with constructors, instance variables, and methods.
 */
public enum RoomType {
    STANDARD(100.0),
    DELUXE(200.0),
    SUITE(350.0);

    private final double basePrice;

    // Constructor for enum
    RoomType(double basePrice) {
        this.basePrice = basePrice;
    }

    public double getBasePrice() {
        return basePrice;
    }

    // Method inside enum to calculate a preliminary base tariff
    public double calculateBaseTariff(int days) {
        return basePrice * days;
    }
}
