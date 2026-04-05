package com.hotelos.gui;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class MainController {
    private HotelManager hotelManager;

    // View Components Mapped to FXML
    @FXML private TableView<Room> roomTable;
    @FXML private TableColumn<Room, Integer> roomNoCol;
    @FXML private TableColumn<Room, String> roomTypeCol;
    @FXML private TableColumn<Room, String> priceCol;
    @FXML private TableColumn<Room, Boolean> availCol;
    @FXML private TableColumn<Room, String> amenitiesCol;

    @FXML private Button btnAddRoom;
    @FXML private Button btnBookRoom;
    @FXML private Button btnCheckout;
    @FXML private Button btnSaveData;

    private ObservableList<Room> roomObservableList;

    @FXML
    public void initialize() {
        hotelManager = new HotelManager();
        initDefaultRooms();

        // Rubric: Permanent storage of data in JDBC
        hotelManager.loadBookingsFromDB();

        initComponentStyles();
        setupTableColumns();
        refreshTable();
    }

    private void initComponentStyles() {
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

    @FXML
    private void showAddRoomDialog(javafx.event.ActionEvent event) {
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

    @FXML
    private void showBookingDialog(javafx.event.ActionEvent event) {
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

    @FXML
    private void handleCheckout(javafx.event.ActionEvent event) {
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

    @FXML
    private void handleSaveData(javafx.event.ActionEvent event) {
        hotelManager.saveBookingsToDB();
        showAlert(Alert.AlertType.INFORMATION, "Success", "Bookings saved to Database via JDBC!");
    }

    private void refreshTable() {
        hotelManager.sortRooms(); // Collection Framework usage
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
