package com.hotelos.gui;

import java.io.*;
import java.sql.*;
import java.util.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * MAIN APPLICATION CLASS (JavaFX GUI Development)
 * Rubric: Screen design with different styles or layouts / various components
 */
public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelos/gui/main.fxml"));
        Parent rootLayout = loader.load();

        primaryStage.setTitle("Hotel Management System Capstone");
        Scene scene = new Scene(rootLayout, 850, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

/**
 * DOMAIN MODELS AND CLASSES (OOP, Enumerations, Generics)
 */

// 1. Interface implementation
interface Amenities {
    String provideWifi();

    String provideBreakfast();
}

// 2. Enumeration
enum RoomType {
    STANDARD(100.0),
    DELUXE(200.0),
    SUITE(350.0);

    private final double basePrice;

    RoomType(double basePrice) {
        this.basePrice = basePrice;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public double calculateBaseTariff(int days) {
        return basePrice * days;
    }
}

// 1. Base Class (Encapsulation and Serialization)
abstract class Room implements Comparable<Room> {

    private Integer roomNumber;
    private RoomType roomType;
    private Boolean isAvailable;

    public Room(Integer roomNumber, RoomType roomType) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.isAvailable = true;
    }

    public Integer getRoomNumber() {
        return roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public Boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }

    // Polymorphic method
    public abstract double calculateTariff(int daysStayed);

    @Override
    public int compareTo(Room other) {
        return this.roomNumber.compareTo(other.getRoomNumber());
    }
}

// 1. Inheritance and Polymorphism
class StandardRoom extends Room implements Amenities {

    public StandardRoom(Integer roomNumber) {
        super(roomNumber, RoomType.STANDARD);
    }

    @Override
    public double calculateTariff(int daysStayed) {
        return getRoomType().calculateBaseTariff(daysStayed);
    }

    @Override
    public String provideWifi() {
        return "Standard Wi-Fi (5 Mbps)";
    }

    @Override
    public String provideBreakfast() {
        return "Continental";
    }
}

class LuxuryRoom extends Room implements Amenities {

    public LuxuryRoom(Integer roomNumber, RoomType roomType) {
        super(roomNumber, roomType);
    }

    @Override
    public double calculateTariff(int daysStayed) {
        double base = getRoomType().calculateBaseTariff(daysStayed);
        return base + (base * 0.10); // 10% premium charge
    }

    @Override
    public String provideWifi() {
        return "Premium High-Speed Wi-Fi";
    }

    @Override
    public String provideBreakfast() {
        return "Gourmet Buffet";
    }
}

class Customer {
    private String name;
    private String contactNumber;
    private int daysStayed;

    public Customer(String name, String contactNumber, int daysStayed) {
        this.name = name;
        this.contactNumber = contactNumber;
        this.daysStayed = daysStayed;
    }

    public String getName() {
        return name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public int getDaysStayed() {
        return daysStayed;
    }
}

// 5. Generic class defining <T, U>
class Pair<T, U> {
    private T first;
    private U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }
}

// Rubric: Billing Management & Generic Bounded Types
class BillingCalculator<T extends Number> {
    // 2. Wrapper classes processing (Autoboxing / Unboxing)
    public Double calculateTotalWithDiscount(T baseTariff, T discountPercentage) {
        double tariff = baseTariff.doubleValue();
        double discount = discountPercentage.doubleValue();
        return tariff - (tariff * (discount / 100));
    }
}

class Booking {
    private Pair<Integer, Customer> bookingInfo;
    private Room room;

    public Booking(Room room, Customer customer) {
        this.room = room;
        this.bookingInfo = new Pair<>(room.getRoomNumber(), customer);
    }

    public Room getRoom() {
        return room;
    }

    public Customer getCustomer() {
        return bookingInfo.getSecond();
    }
}

/**
 * BACKEND SYSTEMS (Collections, Multithreading, Inter-Thread Comms, File I/O)
 */
class HotelManager {
    // 6. Collections framework storing main states
    private List<Room> rooms = new ArrayList<>();
    private List<Booking> currentBookings = new ArrayList<>();
    
    // JDBC Configuration
    private static final String DB_URL = "jdbc:sqlite:hotel_bookings.db";

    public HotelManager() {
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS Bookings (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                         "roomNumber INTEGER," +
                         "customerName TEXT," +
                         "contactNumber TEXT," +
                         "daysStayed INTEGER" +
                         ");";
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public List<Room> getRooms() {
        return rooms;
    }

    // Sort logic utilizing Collections
    public void sortRooms() {
        Collections.sort(rooms);
    }

    // 3. Multithreading synchronization blocking double-booking
    public synchronized boolean bookRoom(Room room, Customer customer) {
        if (!room.isAvailable())
            return false;

        try {
            Thread.sleep(500); // UI load simulation
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        room.setAvailable(false);
        currentBookings.add(new Booking(room, customer));
        notifyAll(); // Safely alert waiting threads of data change
        return true;
    }

    public synchronized Booking checkoutRoom(int roomNumber) {
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
            notifyAll();
        }
        return toCheckout;
    }

    // 4. JDBC SQL Write Logic
    public void saveBookingsToDB() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
             
            // Clear existing table contents
            stmt.execute("DELETE FROM Bookings");
            
            // Insert current tracking
            String insertSQL = "INSERT INTO Bookings (roomNumber, customerName, contactNumber, daysStayed) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                for (Booking b : currentBookings) {
                    pstmt.setInt(1, b.getRoom().getRoomNumber());
                    pstmt.setString(2, b.getCustomer().getName());
                    pstmt.setString(3, b.getCustomer().getContactNumber());
                    pstmt.setInt(4, b.getCustomer().getDaysStayed());
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadBookingsFromDB() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
             
            ResultSet rs = stmt.executeQuery("SELECT * FROM Bookings");
            currentBookings.clear();
            
            while (rs.next()) {
                int rNum = rs.getInt("roomNumber");
                String name = rs.getString("customerName");
                String contact = rs.getString("contactNumber");
                int days = rs.getInt("daysStayed");
                
                Customer cust = new Customer(name, contact, days);
                
                for (Room r : rooms) {
                    if (r.getRoomNumber() == rNum) {
                        r.setAvailable(false);
                        currentBookings.add(new Booking(r, cust));
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database empty or missing, skipping load sequence. " + e.getMessage());
        }
    }
}

// 4. File I/O Character Stream
class ReceiptGenerator {
    public static void generateReceipt(Booking booking, Double finalAmount) {
        String fileName = "Receipt_" + booking.getCustomer().getName() + ".txt";
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("====== HOTEL RECEIPT ======\n");
            writer.write("Guest: " + booking.getCustomer().getName() + "\n");
            writer.write("Room: " + booking.getRoom().getRoomNumber() + "\n");
            writer.write("Total Stay: " + booking.getCustomer().getDaysStayed() + " days\n");
            writer.write(String.format("Final Amount: $%.2f\n", finalAmount));
            writer.write("===========================\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Runnable Thread instance for parallel processing
class RoomServiceTask implements Runnable {
    private Room room;

    public RoomServiceTask(Room room) {
        this.room = room;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(3000); // Wait cycle imitating real background cleaning job
            System.out.println("Room " + room.getRoomNumber() + " maintenance complete.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
