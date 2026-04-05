package com.hotelos.system;

import com.hotelos.models.Booking;
import com.hotelos.models.BillingCalculator;

import java.io.FileWriter;
import java.io.IOException;

/**
 * 4. File I/O & Serialization
 * Uses Character Streams (FileWriter) to generate text receipts.
 */
public class ReceiptGenerator {

    public static void generateReceipt(Booking booking, Double finalAmount) {
        String fileName = "Receipt_" + booking.getCustomer().getName() + "_" + booking.getRoom().getRoomNumber() + ".txt";
        
        // Character Stream (FileWriter)
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("==============================\n");
            writer.write("        HOTEL RECEIPT         \n");
            writer.write("==============================\n");
            writer.write("Guest Name: " + booking.getCustomer().getName() + "\n");
            writer.write("Contact: " + booking.getCustomer().getContactNumber() + "\n");
            writer.write("Room Number: " + booking.getRoom().getRoomNumber() + "\n");
            writer.write("Room Type: " + booking.getRoom().getRoomType() + "\n");
            writer.write("Days Stayed: " + booking.getCustomer().getDaysStayed() + "\n");
            writer.write("------------------------------\n");
            
            // Using Wrapper Classes explicitly here
            Double total = finalAmount;
            writer.write(String.format("Final Amount (After Discount): $%.2f\n", total));
            writer.write("==============================\n");
            writer.write("Thank you for staying with us!\n");
            
            System.out.println("Receipt generated successfully: " + fileName);
        } catch (IOException e) {
            System.err.println("Error generating receipt: " + e.getMessage());
        }
    }
}
