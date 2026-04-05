module com.hotelos.gui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.hotelos.gui to javafx.fxml;
    exports com.hotelos.gui;
}
