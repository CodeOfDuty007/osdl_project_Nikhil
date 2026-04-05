package com.hotelos.models;

import java.io.Serializable;

/**
 * 1. Object-Oriented Programming (OOP)
 * Abstract base class Room implementing Encapsulation and Serializable.
 */
public abstract class Room implements Serializable, Comparable<Room> {
    private static final long serialVersionUID = 1L;

    // Encapsulation using private fields
    private Integer roomNumber;
    private RoomType roomType;
    private Boolean isAvailable;

    public Room(Integer roomNumber, RoomType roomType) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.isAvailable = true;
    }

    // Public Getters and Setters (Encapsulation)
    public Integer getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(Integer roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public Boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }

    // Abstract method to demonstrate polymorphism
    public abstract double calculateTariff(int daysStayed);

    @Override
    public int compareTo(Room other) {
        return this.roomNumber.compareTo(other.getRoomNumber());
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomNumber=" + roomNumber +
                ", roomType=" + roomType +
                ", isAvailable=" + isAvailable +
                '}';
    }
}
