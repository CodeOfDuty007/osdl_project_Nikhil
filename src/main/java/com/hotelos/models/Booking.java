package com.hotelos.models;

import java.io.Serializable;

/**
 * A Booking links a Customer to a Room and stores the reservation details.
 * Also utilizes the generic Pair class.
 */
public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    // Pair associating a room number (Integer) with Guest details (Customer)
    private Pair<Integer, Customer> bookingInfo;
    private Room room;

    public Booking(Room room, Customer customer) {
        this.room = room;
        this.bookingInfo = new Pair<>(room.getRoomNumber(), customer);
    }

    public Pair<Integer, Customer> getBookingInfo() {
        return bookingInfo;
    }

    public Room getRoom() {
        return room;
    }

    public Customer getCustomer() {
        return bookingInfo.getSecond();
    }
}
