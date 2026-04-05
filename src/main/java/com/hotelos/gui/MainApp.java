package com.hotelos.gui;

import com.hotelos.models.*;
import com.hotelos.system.HotelManager;
import com.hotelos.system.ReceiptGenerator;
import com.hotelos.system.RoomServiceTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.BlurType;
import javafx.scene.paint.Color;
import javafx.scene.Cursor;

import java.util.Optional;

/**
 * 7. JavaFX GUI Development
 * Primary entry point. Includes VBox, HBox, GridPane, and Event Handlers.
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
        initDefaultRooms(); // Add some dummy rooms
        
        // 4. File I/O & Serialization (Load data if exists)
        hotelManager.loadBookingsFromFile();

        primaryStage.setTitle("Hotel Management System Capstone");

        // 7. GUI Elements: VBox for main layout
        VBox rootLayout = new VBox(20);
        rootLayout.setPadding(new Insets(30));
        rootLayout.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8f9fa, #e9ecef);");

        // Title Label
        Label titleLabel = new Label("Grand Hotel Management System");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        titleLabel.setStyle("-fx-text-fill: #1a252f; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 5, 0, 0, 2);");
        titleLabel.setAlignment(Pos.CENTER);

        // Room TableView
        createRoomTable();

        // 7. GUI Elements: HBox/GridPane for navigation & actions
        HBox topBarBox = new HBox(15);
        topBarBox.setAlignment(Pos.CENTER);

        Button btnAddRoom = new Button("Add Room");
        Button btnBookRoom = new Button("Book Selected Room");
        Button btnCheckout = new Button("Checkout & Receipt");
        Button btnSaveData = new Button("Save Data");

        // Set styles using helper for modern effects
        java.util.function.BiConsumer<Button, String> styleBtn = (btn, color) -> {
            String baseStyle = "-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20 10 20; -fx-background-radius: 8;";
            String hoverStyle = "-fx-background-color: derive(" + color + ", -10%); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20 10 20; -fx-background-radius: 8;";
            btn.setStyle(baseStyle);
            btn.setCursor(Cursor.HAND);
            DropShadow dropShadow = new DropShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.2), 10, 0.1, 0, 5);
            btn.setEffect(dropShadow);
            btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
            btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        };
        styleBtn.accept(btnAddRoom, "#3498db");
        styleBtn.accept(btnBookRoom, "#2ecc71");
        styleBtn.accept(btnCheckout, "#e74c3c");
        styleBtn.accept(btnSaveData, "#9b59b6");

        topBarBox.getChildren().addAll(btnAddRoom, btnBookRoom, btnCheckout, btnSaveData);

        // Add event handlers
        btnAddRoom.setOnAction(e -> showAddRoomDialog());
        btnBookRoom.setOnAction(e -> showBookingDialog());
        btnCheckout.setOnAction(e -> handleCheckout());
        btnSaveData.setOnAction(e -> {
            hotelManager.saveBookingsToFile();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Bookings serialized and saved to file!");
        });

        rootLayout.getChildren().addAll(titleLabel, topBarBox, roomTable);

        Scene scene = new Scene(rootLayout, 850, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Populate the datatable dynamically upon startup
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
        roomNoCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getRoomNumber()).asObject());

        TableColumn<Room, String> roomTypeCol = new TableColumn<>("Type");
        roomTypeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoomType().name()));

        TableColumn<Room, String> priceCol = new TableColumn<>("Price/Night");
        priceCol.setCellValueFactory(cellData -> new SimpleStringProperty("$" + cellData.getValue().getRoomType().getBasePrice()));

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
        hotelManager.sortRooms(); // 6. Collections sort
        roomObservableList.setAll(hotelManager.getRooms());
        roomTable.setItems(roomObservableList);
        roomTable.refresh();
    }

    private void showAddRoomDialog() {
        Dialog<Room> dialog = new Dialog<>();
        dialog.setTitle("Add New Room");
        dialog.setHeaderText("Enter Room Details");

        // 7. GUI Elements: GridPane layout
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
        // Apply aesthetic effect to image
        DropShadow imageShadow = new DropShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.3), 10, 0.1, 0, 5);
        roomImageView.setEffect(imageShadow);

        Runnable updatePhoto = () -> {
            String imgUrl;
            switch(typeBox.getValue()) {
                case DELUXE:
                    imgUrl = "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=400&q=80";
                    break;
                case SUITE:
                    imgUrl = "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=400&q=80";
                    break;
                default: // STANDARD
                    imgUrl = "https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=400&q=80";
                    break;
            }
            roomImageView.setImage(new Image(imgUrl, true)); // background loading
        };
        typeBox.setOnAction(e -> updatePhoto.run());
        updatePhoto.run();

        grid.add(new Label("Room Number:"), 0, 0);
        grid.add(roomNoField, 1, 0);
        grid.add(new Label("Room Type:"), 0, 1);
        grid.add(typeBox, 1, 1);
        
        VBox imageContainer = new VBox(new Label("Room Preview:"), roomImageView);
        imageContainer.setSpacing(10);
        grid.add(imageContainer, 2, 0, 1, 3); // Spans over to the right side

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

        Optional<Room> result = dialog.showAndWait();
        result.ifPresent(room -> {
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
        dialog.setHeaderText("Enter Customer Details");

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

        Optional<Customer> result = dialog.showAndWait();
        result.ifPresent(customer -> {
            // Book using multithreaded logic (simulation via Thread for background)
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
        if (selectedRoom == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a room to check out.");
            return;
        }

        if (selectedRoom.isAvailable()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "This room is not currently booked.");
            return;
        }

        // Checkout process
        Booking toCheckout = hotelManager.checkoutRoom(selectedRoom.getRoomNumber());
        if (toCheckout != null) {
            // Calculate Billing Using Wrapper Classes and Generics
            BillingCalculator<Double> calculator = new BillingCalculator<>();
            Double baseTariff = selectedRoom.calculateTariff(toCheckout.getCustomer().getDaysStayed());
            Double discount = 5.0; // 5% discount for demo
            
            Double finalBill = calculator.calculateTotalWithDiscount(baseTariff, discount);

            // Generate File I/O Receipt
            ReceiptGenerator.generateReceipt(toCheckout, finalBill);

            // Multithreading background task - cleaning room
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
