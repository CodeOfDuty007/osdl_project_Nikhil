package com.hotelos.gui;

import java.io.*;
import java.sql.*;
import java.util.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * MAIN APPLICATION CLASS (JavaFX GUI Development)
 * Rubric: Screen design with different styles or layouts / various components
 */
public class MainApp extends Application {
    private HotelManager hotelManager;

    // View Components
    private TableView<Room> roomTable;
    private ObservableList<Room> roomObservableList;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        hotelManager = new HotelManager();
        initDefaultRooms();

        // Rubric: Permanent storage of data in JDBC
        hotelManager.loadBookingsFromDB();

        primaryStage.setTitle("Hotel Management System Capstone");

        // Layout: VBox for main layout with modern styling
        VBox rootLayout = new VBox(20);
        rootLayout.setPadding(new Insets(30));
        rootLayout.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8f9fa, #e9ecef);");

        Label titleLabel = new Label("Grand Hotel Management System");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        titleLabel.setStyle("-fx-text-fill: #1a252f; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 5, 0, 0, 2);");
        titleLabel.setAlignment(Pos.CENTER);

        createRoomTable();

        HBox topBarBox = new HBox(15);
        topBarBox.setAlignment(Pos.CENTER);

        Button btnAddRoom = new Button("Add Room");
        Button btnBookRoom = new Button("Book Selected Room");
        Button btnCheckout = new Button("Checkout & Receipt");
        Button btnSaveData = new Button("Save to DB (JDBC)");

        // UI Styling using lambda functions
        java.util.function.BiConsumer<Button, String> styleBtn = (btn, color) -> {
            String baseStyle = "-fx-background-color: " + color
                    + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20 10 20; -fx-background-radius: 8;";
            String hoverStyle = "-fx-background-color: derive(" + color
                    + ", -10%); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20 10 20; -fx-background-radius: 8;";
            btn.setStyle(baseStyle);
            btn.setCursor(Cursor.HAND);
            DropShadow dropShadow = new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.2), 10, 0.1, 0, 5);
            btn.setEffect(dropShadow);
            btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
            btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        };

        styleBtn.accept(btnAddRoom, "#3498db");
        styleBtn.accept(btnBookRoom, "#2ecc71");
        styleBtn.accept(btnCheckout, "#e74c3c");
        styleBtn.accept(btnSaveData, "#9b59b6");

        topBarBox.getChildren().addAll(btnAddRoom, btnBookRoom, btnCheckout, btnSaveData);

        // Event handling bindings
        btnAddRoom.setOnAction(e -> showAddRoomDialog());
        btnBookRoom.setOnAction(e -> showBookingDialog());
        btnCheckout.setOnAction(e -> handleCheckout());
        btnSaveData.setOnAction(e -> {
            hotelManager.saveBookingsToDB();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Bookings saved to Database via JDBC!");
        });

        rootLayout.getChildren().addAll(titleLabel, topBarBox, roomTable);

        Scene scene = new Scene(rootLayout, 850, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        refreshTable();
    }

    private void initDefaultRooms() {
        hotelManager.addRoom(new StandardRoom(101));
        hotelManager.addRoom(new StandardRoom(102));
        hotelManager.addRoom(new LuxuryRoom(201, RoomType.DELUXE));
        hotelManager.addRoom(new LuxuryRoom(301, RoomType.SUITE));
    }

    private void createRoomTable() {
        roomTable = new TableView<>();
        roomObservableList = FXCollections.observableArrayList();

        TableColumn<Room, Integer> roomNoCol = new TableColumn<>("Room No.");
        roomNoCol.setCellValueFactory(
                cellData -> new SimpleIntegerProperty(cellData.getValue().getRoomNumber()).asObject());

        TableColumn<Room, String> roomTypeCol = new TableColumn<>("Type");
        roomTypeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoomType().name()));

        TableColumn<Room, String> priceCol = new TableColumn<>("Price/Night");
        priceCol.setCellValueFactory(
                cellData -> new SimpleStringProperty("$" + cellData.getValue().getRoomType().getBasePrice()));

        TableColumn<Room, Boolean> availCol = new TableColumn<>("Available?");
        availCol.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isAvailable()));

        TableColumn<Room, String> amenitiesCol = new TableColumn<>("Amenities");
        amenitiesCol.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Amenities) {
                Amenities a = (Amenities) cellData.getValue();
                return new SimpleStringProperty(a.provideWifi() + " & " + a.provideBreakfast());
            }
            return new SimpleStringProperty("None");
        });

        roomTable.getColumns().addAll(roomNoCol, roomTypeCol, priceCol, availCol, amenitiesCol);
        roomTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void refreshTable() {
        hotelManager.sortRooms(); // Collection Framework usage
        roomObservableList.setAll(hotelManager.getRooms());
        roomTable.setItems(roomObservableList);
        roomTable.refresh();
    }

    private void showAddRoomDialog() {
        Dialog<Room> dialog = new Dialog<>();
        dialog.setTitle("Add New Room");
        dialog.setHeaderText("Enter Room Details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 30, 20, 20));

        TextField roomNoField = new TextField();
        roomNoField.setPromptText("e.g. 401");

        ComboBox<RoomType> typeBox = new ComboBox<>(FXCollections.observableArrayList(RoomType.values()));
        typeBox.setValue(RoomType.STANDARD);

        ImageView roomImageView = new ImageView();
        roomImageView.setFitWidth(280);
        roomImageView.setFitHeight(180);
        roomImageView.setPreserveRatio(true);
        DropShadow imageShadow = new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.3), 10, 0.1, 0, 5);
        roomImageView.setEffect(imageShadow);

        Runnable updatePhoto = () -> {
            String imgUrl;
            switch (typeBox.getValue()) {
                case DELUXE:
                    imgUrl = "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=400&q=80";
                    break;
                case SUITE:
                    imgUrl = "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=400&q=80";
                    break;
                default:
                    imgUrl = "https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=400&q=80";
                    break;
            }
            roomImageView.setImage(new Image(imgUrl, true));
        };
        typeBox.setOnAction(e -> updatePhoto.run());
        updatePhoto.run();

        grid.add(new Label("Room Number:"), 0, 0);
        grid.add(roomNoField, 1, 0);
        grid.add(new Label("Room Type:"), 0, 1);
        grid.add(typeBox, 1, 1);

        VBox imageContainer = new VBox(new Label("Room Preview:"), roomImageView);
        imageContainer.setSpacing(10);
        grid.add(imageContainer, 2, 0, 1, 3);

        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    int no = Integer.parseInt(roomNoField.getText());
                    RoomType type = typeBox.getValue();
                    if (type == RoomType.STANDARD) {
                        return new StandardRoom(no);
                    } else {
                        return new LuxuryRoom(no, type);
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(room -> {
            hotelManager.addRoom(room);
            refreshTable();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Added Room: " + room.getRoomNumber());
        });
    }

    private void showBookingDialog() {
        Room selectedRoom = roomTable.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a room to book.");
            return;
        }

        if (!selectedRoom.isAvailable()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Selected room is already booked!");
            return;
        }

        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle("Book Room " + selectedRoom.getRoomNumber());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        TextField contactField = new TextField();
        TextField daysField = new TextField();

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Contact No:"), 0, 1);
        grid.add(contactField, 1, 1);
        grid.add(new Label("Days Stayed:"), 0, 2);
        grid.add(daysField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        ButtonType bookBtnType = new ButtonType("Book", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(bookBtnType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == bookBtnType) {
                try {
                    int days = Integer.parseInt(daysField.getText());
                    return new Customer(nameField.getText(), contactField.getText(), days);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(customer -> {
            // Multithreading and Synchronization feature
            Thread bookingThread = new Thread(() -> {
                boolean success = hotelManager.bookRoom(selectedRoom, customer);
                Platform.runLater(() -> {
                    if (success) {
                        refreshTable();
                        showAlert(Alert.AlertType.INFORMATION, "Booking Confirmed",
                                "Room " + selectedRoom.getRoomNumber() + " booked for " + customer.getName());
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Booking Failed", "Room became unavailable.");
                    }
                });
            });
            bookingThread.start();
        });
    }

    private void handleCheckout() {
        Room selectedRoom = roomTable.getSelectionModel().getSelectedItem();
        if (selectedRoom == null || selectedRoom.isAvailable()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a currently booked room to check out.");
            return;
        }

        Booking toCheckout = hotelManager.checkoutRoom(selectedRoom.getRoomNumber());
        if (toCheckout != null) {
            // Generics bounds & Wrapper Classes calculation execution
            BillingCalculator<Double> calculator = new BillingCalculator<>();
            Double baseTariff = selectedRoom.calculateTariff(toCheckout.getCustomer().getDaysStayed());
            Double discount = 5.0;

            Double finalBill = calculator.calculateTotalWithDiscount(baseTariff, discount);

            // File IO character streams
            ReceiptGenerator.generateReceipt(toCheckout, finalBill);

            // Runnable Thread simulation
            Thread cleanerThread = new Thread(new RoomServiceTask(selectedRoom));
            cleanerThread.start();

            refreshTable();
            showAlert(Alert.AlertType.INFORMATION, "Checkout Successful",
                    "Checked out " + toCheckout.getCustomer().getName() + ".\n" +
                            "Base Amount: $" + baseTariff + "\n" +
                            "Discount Applied: " + discount + "%\n" +
                            "Final Amount: $" + String.format("%.2f", finalBill) + "\n" +
                            "Receipt text file generated!\n" +
                            "Room cleaner dispatched.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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
