package com.example.demo4;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.Core;

import java.io.IOException;

public class Application extends javafx.application.Application {
    private MainController controller;
    @Override
    public void start(Stage stage) throws IOException {
        try {
            //Thêm fxml
            FXMLLoader loader =new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Truyền tham chiếu của stage vào MainController
            controller = loader.getController();
            controller.setStage(stage);

            //thêm css
            scene.getStylesheets().add(getClass().getResource("takeAPhoto.css").toExternalForm());
            scene.getStylesheets().forEach(System.out::println);

            stage.setTitle("Test Frontend");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void stop() {
        // Giải phóng tài nguyên tại đây
        if (controller != null) {
            controller.releaseResources();
        }
        System.out.println("Application is closing...");
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch();

    }
}
