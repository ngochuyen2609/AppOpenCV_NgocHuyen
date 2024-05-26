package com.example.demo4;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class FilterImageController {

    private Stage stage;
    private Image image;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setImage (Image image) {
        this.image = image;
    }

    public void chooseFilter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("image-view.fxml"));
            Parent root = loader.load();

            ImageViewController controller = loader.getController();
            controller.setStage(stage);
            controller.setImage(image); // Sử dụng ảnh gần nhất
            Scene scene = new Scene(root);

            // Thêm CSS
            scene.getStylesheets().add(getClass().getResource("image.css").toExternalForm());
            stage.setTitle("Test Filter");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clickFilter1(ActionEvent event) {
        chooseFilter();
    }

    public void clickFilter2(ActionEvent event) {
        chooseFilter();
    }

    public void clickFilter3(ActionEvent event) {
        chooseFilter();
    }

    public void clickFilter4(ActionEvent event) {
        chooseFilter();
    }
}

