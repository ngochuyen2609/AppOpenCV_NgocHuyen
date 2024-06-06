package com.example.demo4;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ImageViewController extends ChooseImage {

    @FXML
    private ImageView image;
    @FXML
    private StackPane image_layout;
    private static Image lastImage;

    public StackPane getImage_layout() {
        return image_layout;
    }


    public ImageView getImage() {
        return image;
    }

    public void setLastImage(Image a){
        lastImage = a;
    }

    public  Image getLatestImage() {
        return lastImage; // Trả về ảnh gần nhất
    }

    // Sự kiện quay lại cảnh trước
    public void backScene(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.setStage(stage);

            Scene scene = new Scene(root);

            // Thêm CSS
            scene.getStylesheets().add(getClass().getResource("takeAPhoto.css").toExternalForm());

            stage.setTitle("Test Frontend");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void clickFilter (ActionEvent event) throws IOException {

        if( lastImage == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("You don't choose Image");
            alert.showAndWait();
        }
        else {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("filterImage-view.fxml"));
            Parent root = loader.load();

            //add controller
            FilterImageController controller = loader.getController();
            //set thêm ảnh

            Scene newScene = new Scene(root);
            stage.setScene(newScene);
            stage.setTitle("Select Filter");
            //thêm css mới cho scene này
            newScene.getStylesheets().add(getClass().getResource("filter.css").toExternalForm());
            controller.setStage(stage);
            stage.show();;
        }
    }

}