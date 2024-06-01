package com.example.demo4;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.opencv.objdetect.Objdetect.CASCADE_SCALE_IMAGE;

public class FilterController extends Filter {
    @Override
    public void chooseFilter() {
        try {
            // Thêm fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Truyền tham chiếu của stage vào MainController
            MainController controller = loader.getController();
            controller.setStage(stage);  // Pass the stage to the MainController
            controller.setSticker(this.sticker);
            controller.setIsFilterValue(true);
            controller.startStopCamera(new ActionEvent());
            // Thêm css
            scene.getStylesheets().add(getClass().getResource("takeAPhoto.css").toExternalForm());

            stage.setTitle("Test Frontend");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
