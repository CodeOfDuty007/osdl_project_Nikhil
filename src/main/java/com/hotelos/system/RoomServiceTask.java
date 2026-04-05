package com.hotelos.system;

import com.hotelos.models.Room;

/**
 * 3. Multithreading & Synchronization
 * Implement Multithreading using the Runnable interface.
 */
public class RoomServiceTask implements Runnable {
    private Room room;

    public RoomServiceTask(Room room) {
        this.room = room;
    }

    @Override
    public void run() {
        System.out.println("Starting background room cleaning and maintenance for Room: " + room.getRoomNumber() + " on thread: " + Thread.currentThread().getName());
        try {
            // Simulate work (e.g., room cleaning)
            Thread.sleep(3000); // 3 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Room service interrupted.");
        }
        System.out.println("Room cleaning completed for Room: " + room.getRoomNumber());
        
        // At this point, you could make the room available if it wasn't already.
    }
}
