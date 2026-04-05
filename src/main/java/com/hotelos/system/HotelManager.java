package com.hotelos.system;

import com.hotelos.models.Booking;
import com.hotelos.models.Customer;
import com.hotelos.models.Room;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Core business logic class demonstrating Concurrency, Serialization, Generics, and Collections.
 */
public class HotelManager {
    // 6. Collections Framework (ArrayList)
    private List<Room> rooms;
    private List<Booking> currentBookings;

    private static final String BOOKING_FILE = "bookings.dat";

    public HotelManager() {
        rooms = new ArrayList<>();
        currentBookings = new ArrayList<>();
    }

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public List<Booking> getCurrentBookings() {
        return currentBookings;
    }

    /**
     * 6. Collections Framework (Collections.sort using Comparable interface in Room class)
     */
    public void sortRooms() {
        Collections.sort(rooms);
    }

    /**
     * 5. Generics
     * Generic method to display any type of list.
     */
    public <T> void displayData(List<T> list) {
        // 6. Collections Framework (Iterator)
        Iterator<T> iterator = list.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next().toString());
        }
    }

    /**
     * 3. Multithreading & Synchronization
     * Use synchronization block/methods to prevent double-booking.
     */
    public synchronized boolean bookRoom(Room room, Customer customer) {
        if (!room.isAvailable()) {
            // Already booked
            return false;
        }

        // Simulating booking processing delay safely
        try {
            Thread.sleep(500); // 0.5s delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        room.setAvailable(false);
        Booking newBooking = new Booking(room, customer);
        currentBookings.add(newBooking);

        System.out.println("Booking successful for room: " + room.getRoomNumber());
        notifyAll(); // Notify any waiting threads that a room state has changed (optional usage)
        return true;
    }

    public synchronized Booking checkoutRoom(int roomNumber) {
        // Find the booking
        Booking toCheckout = null;
        for (Booking b : currentBookings) {
            if (b.getRoom().getRoomNumber() == roomNumber) {
                toCheckout = b;
                break;
            }
        }

        if (toCheckout != null) {
            toCheckout.getRoom().setAvailable(true);
            currentBookings.remove(toCheckout);
            notifyAll(); // Notify any thread waiting for a room to become available
        }
        return toCheckout;
    }

    /**
     * 3. Multithreading & Synchronization (wait/notify mechanism usage)
     */
    public synchronized Room waitForAvailableRoom() {
        while (true) {
            for (Room r : rooms) {
                if (r.isAvailable()) return r;
            }
            try {
                // If no rooms are available, wait until someone checks out
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
    }

    /**
     * 4. File I/O & Serialization
     * Object Serialization to a file.
     */
    public void saveBookingsToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BOOKING_FILE))) {
            oos.writeObject(currentBookings);
            System.out.println("Bookings serialized to " + BOOKING_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 4. File I/O & Serialization
     * Object Deserialization from a file.
     */
    @SuppressWarnings("unchecked")
    public void loadBookingsFromFile() {
        File file = new File(BOOKING_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            currentBookings = (List<Booking>) ois.readObject();
            
            // Mark those rooms as unavailable since they are booked
            for(Booking b : currentBookings) {
                for(Room r : rooms) {
                    if(r.getRoomNumber().equals(b.getRoom().getRoomNumber())) {
                        r.setAvailable(false);
                    }
                }
            }
            System.out.println("Bookings deserialized from " + BOOKING_FILE);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
