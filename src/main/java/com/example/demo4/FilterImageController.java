package com.example.demo4;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class FilterImageController {

    private Stage stage;
    @FXML
    private ImageView filter1;
    @FXML
    private ImageView filter2;
    @FXML
    private ImageView filter3;
    @FXML
    private ImageView filter4;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setImage (Image image) {
        filter1.setImage(image);
        filter2.setImage(image);
        filter3.setImage(image);
        filter4.setImage(image);
    }

    public void chooseFilter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("image-view.fxml"));
            Parent root = loader.load();

            ImageViewController controller = loader.getController();
            controller.setStage(stage);
            Scene newScene = new Scene(root);

            Image resultImage =controller.getLatestImage();

            ZoomableImageView zoomableImageView = new ZoomableImageView();
            zoomableImageView.setImage(resultImage); // Đặt hình ảnh cho ZoomableImageView
            zoomableImageView.fitWidthProperty().bind(controller.getImage_layout().widthProperty());
            zoomableImageView.fitHeightProperty().bind(controller.getImage_layout().heightProperty());
            zoomableImageView.setPreserveRatio(true);

            controller.getImage_layout().getChildren().add(zoomableImageView);


            newScene.getStylesheets().add(getClass().getResource("image.css").toExternalForm());
            stage.setScene(newScene);
            stage.setTitle("Selected Image");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void clickFilter1(ActionEvent event) {
        chooseFilter();
    }
    @FXML
    public void clickFilter2(ActionEvent event) {
        chooseFilter();
    }
    @FXML
    public void clickFilter3(ActionEvent event) {
        chooseFilter();
    }
    @FXML
    public void clickFilter4(ActionEvent event) {
        chooseFilter();
    }
}

