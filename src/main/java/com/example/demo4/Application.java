package com.example.demo4;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        try {
            //Thêm fxml
            FXMLLoader loader =new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Truyền tham chiếu của stage vào MainController
            MainController controller = loader.getController();
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

    public static void main(String[] args) {
        launch();
    }
}