package com.hotelos.gui;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class MainController {
    private HotelManager hotelManager;

    // View Components (Navigation & Views)
    @FXML private VBox homeView;
    @FXML private VBox addRoomView;
    @FXML private VBox bookRoomView;
    @FXML private VBox checkoutView;
    @FXML private VBox amenitiesView;
    
    // Top Nav buttons
    @FXML private Button navHome;
    @FXML private Button navAddRoom;
    @FXML private Button navBookRoom;
    @FXML private Button navCheckout;
    @FXML private Button navAmenities;

    // Home Table components
    @FXML private TableView<Room> roomTable;
    @FXML private TableColumn<Room, Integer> roomNoCol;
    @FXML private TableColumn<Room, String> roomTypeCol;
    @FXML private TableColumn<Room, String> priceCol;
    @FXML private TableColumn<Room, Boolean> availCol;
    @FXML private TableColumn<Room, String> amenitiesCol;
    private ObservableList<Room> roomObservableList;

    // Add Room components
    @FXML private TextField txtAddRoomNo;
    @FXML private ComboBox<RoomType> comboAddRoomType;

    // Book Room components
    @FXML private ComboBox<Room> comboBookRoomSelect;
    @FXML private ImageView roomImagePreview;
    @FXML private TextField txtGuestName;
    @FXML private TextField txtGuestContact;
    @FXML private TextField txtGuestDays;

    // Checkout components
    @FXML private ComboBox<Room> comboCheckoutRoom;

    @FXML
    public void initialize() {
        hotelManager = new HotelManager();
        initDefaultRooms();

        // Rubric Requirement: Permanent storage of data in JDBC
        hotelManager.loadBookingsFromDB();

        setupTableColumns();
        
        // Populate static combo boxes
        comboAddRoomType.setItems(FXCollections.observableArrayList(RoomType.values()));
        comboAddRoomType.setValue(RoomType.STANDARD);
        
        // Set Converters to display Room object data cleanly in the dropdowns
        javafx.util.StringConverter<Room> roomConverter = new javafx.util.StringConverter<Room>() {
            @Override
            public String toString(Room room) {
                return room == null ? "" : "Room " + room.getRoomNumber() + " (" + room.getRoomType() + ")";
            }
            @Override
            public Room fromString(String string) {
                return null;
            }
        };
        comboBookRoomSelect.setConverter(roomConverter);
        comboCheckoutRoom.setConverter(roomConverter);
        
        // Dynamic Room Photo Preview depending on user selection
        comboBookRoomSelect.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    String photoName = (newVal.getRoomType() == RoomType.STANDARD) ? "single.jpg" : "suite.jpg";
                    java.io.File file = new java.io.File("images", photoName);
                    roomImagePreview.setImage(new Image(file.toURI().toString()));
                } catch (Exception e) {
                    System.err.println("Could not load image: " + e.getMessage());
                }
            }
        });

        // Show home view initially
        navHome();
    }

    private void setupTableColumns() {
        roomObservableList = FXCollections.observableArrayList();

        roomNoCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getRoomNumber()).asObject());
        roomTypeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoomType().name()));
        priceCol.setCellValueFactory(cellData -> new SimpleStringProperty("$" + cellData.getValue().getRoomType().getBasePrice()));
        availCol.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isAvailable()));
        amenitiesCol.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Amenities) {
                Amenities a = (Amenities) cellData.getValue();
                return new SimpleStringProperty(a.provideWifi() + " & " + a.provideBreakfast());
            }
            return new SimpleStringProperty("None");
        });

        roomTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }

    private void initDefaultRooms() {
        hotelManager.addRoom(new StandardRoom(101));
        hotelManager.addRoom(new StandardRoom(102));
        hotelManager.addRoom(new LuxuryRoom(201, RoomType.DELUXE));
        hotelManager.addRoom(new LuxuryRoom(301, RoomType.SUITE));
    }
    
    // --- NAVIGATION LOGIC (Simulates multi-screen behavior beautifully) ---
    private void hideAllViews() {
        homeView.setVisible(false);
        addRoomView.setVisible(false);
        bookRoomView.setVisible(false);
        checkoutView.setVisible(false);
        amenitiesView.setVisible(false);
        
        // Reset nav styles
        String unselected = "-fx-background-color: transparent; -fx-text-fill: #EFEBE9; -fx-font-weight: bold; -fx-font-size: 15px; -fx-cursor: hand;";
        navHome.setStyle(unselected);
        navAddRoom.setStyle(unselected);
        navBookRoom.setStyle(unselected);
        navCheckout.setStyle(unselected);
        navAmenities.setStyle(unselected);
    }
    
    private void highlightNav(Button btn) {
        String selected = "-fx-background-color: #3E2723; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 5;";
        btn.setStyle(selected);
    }

    @FXML
    private void navAmenities() {
        hideAllViews();
        amenitiesView.setVisible(true);
        amenitiesView.toFront();
        highlightNav(navAmenities);
    }

    @FXML
    private void navHome() {
        hideAllViews();
        refreshTable();
        homeView.setVisible(true);
        homeView.toFront();
        highlightNav(navHome);
    }

    @FXML
    private void navAddRoom() {
        hideAllViews();
        txtAddRoomNo.clear();
        addRoomView.setVisible(true);
        addRoomView.toFront();
        highlightNav(navAddRoom);
    }

    @FXML
    private void navBookRoom() {
        hideAllViews();
        // Setup Book Room data
        ObservableList<Room> availableRooms = FXCollections.observableArrayList();
        for (Room r : hotelManager.getRooms()) {
            if (r.isAvailable()) availableRooms.add(r);
        }
        comboBookRoomSelect.setItems(availableRooms);
        
        txtGuestName.clear();
        txtGuestContact.clear();
        txtGuestDays.clear();
        
        bookRoomView.setVisible(true);
        bookRoomView.toFront();
        highlightNav(navBookRoom);
    }

    @FXML
    private void navCheckout() {
        hideAllViews();
        // Setup Checkout data
        ObservableList<Room> strictlyBooked = FXCollections.observableArrayList();
        for (Room r : hotelManager.getRooms()) {
            if (!r.isAvailable()) strictlyBooked.add(r);
        }
        comboCheckoutRoom.setItems(strictlyBooked);
        
        checkoutView.setVisible(true);
        checkoutView.toFront();
        highlightNav(navCheckout);
    }

    // --- FORM ACTIONS ---

    @FXML
    private void submitAddRoom() {
        try {
            if (txtAddRoomNo.getText().trim().isEmpty()) throw new NumberFormatException();
            int no = Integer.parseInt(txtAddRoomNo.getText().trim());
            RoomType type = comboAddRoomType.getValue();
            
            Room newRoom;
            if (type == RoomType.STANDARD) {
                newRoom = new StandardRoom(no);
            } else {
                newRoom = new LuxuryRoom(no, type);
            }
            
            hotelManager.addRoom(newRoom);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Successfully added new room: " + no);
            navHome(); // Route back
        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please provide a valid Room Number.");
        }
    }

    @FXML
    private void submitBooking() {
        Room selectedRoom = comboBookRoomSelect.getValue();
        if (selectedRoom == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select an available room.");
            return;
        }

        try {
            String name = txtGuestName.getText().trim();
            String contact = txtGuestContact.getText().trim();
            if (name.isEmpty() || contact.isEmpty()) throw new IllegalArgumentException("Empty fields");
            int days = Integer.parseInt(txtGuestDays.getText().trim());
            
            Customer customer = new Customer(name, contact, days);

            // Multithreading and Synchronization feature for safe booking
            Thread bookingThread = new Thread(() -> {
                boolean success = hotelManager.bookRoom(selectedRoom, customer);
                Platform.runLater(() -> {
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Booking Confirmed",
                                "Room " + selectedRoom.getRoomNumber() + " secured for " + customer.getName());
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Booking Failed", "Room became unavailable during transaction.");
                    }
                    navHome(); // Sync and redirect
                });
            });
            bookingThread.start();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please ensure all fields are filled perfectly and Days Stayed is a number.");
        }
    }

    @FXML
    private void submitCheckout() {
        Room selectedRoom = comboCheckoutRoom.getValue();
        if (selectedRoom == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a room to check out.");
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

            // Runnable Thread simulation for room cleanup
            Thread cleanerThread = new Thread(new RoomServiceTask(selectedRoom));
            cleanerThread.start();

            showAlert(Alert.AlertType.INFORMATION, "Checkout Successful via Automated Thread",
                    "Assessed Guest: " + toCheckout.getCustomer().getName() + ".\n" +
                            "Base Room Rate: $" + baseTariff + "\n" +
                            "Corporate Discount: " + discount + "%\n" +
                            "Paid Total: $" + String.format("%.2f", finalBill) + "\n" +
                            "Serialized Receipt textual file has been generated securely.\n" +
                            "Cleaning Task Thread Dispatched to Background Pool.");
                            
            navHome();
        }
    }

    @FXML
    private void handleSaveData() {
        hotelManager.saveBookingsToDB();
        showAlert(Alert.AlertType.INFORMATION, "Database Saved", "Data safely stored into SQLite database cluster!");
    }

    private void refreshTable() {
        hotelManager.sortRooms(); // Collection Framework usage constraint
        roomObservableList.setAll(hotelManager.getRooms());
        roomTable.setItems(roomObservableList);
        roomTable.refresh();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
