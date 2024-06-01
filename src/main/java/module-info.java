module com.example.demo4 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires opencv;
    requires javafx.swing;
    requires java.logging;
    opens com.example.demo4 to javafx.fxml;
    exports com.example.demo4;
}