package com.example.demo4;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.opencv.objdetect.Objdetect.CASCADE_SCALE_IMAGE;

public class StartStopCamera {

    @FXML
    protected ImageView originalFrame;
    protected Stage stage;
    @FXML
    protected Button cameraButton;
    @FXML
    protected Label lblnumber;

    protected CascadeClassifier faceDetector;
    protected boolean isCameraActive = false;
    protected VideoCapture cameraCapture;


    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    protected void startStopCamera(ActionEvent event) {
        if (!isCameraActive) {
            startCamera();
            cameraButton.setText("Stop");
        } else {
            stopCamera();
            cameraButton.setText("Start");
            lblnumber.setText("Person Number");
        }
    }

    protected void startCamera() {
        cameraCapture = new VideoCapture(0);
        Mat frame = new Mat();

        isCameraActive = true;

        new Thread(() -> {
            while (cameraCapture.read(frame)) {
                if (frame.empty()) {
                    System.out.println("No detection");
                    break;
                } else {
                    try {
                        Platform.runLater(() -> {
                            MatOfByte mem = new MatOfByte();
                            Imgcodecs.imencode(".bmp", frame, mem);
                            InputStream in = new ByteArrayInputStream(mem.toArray());
                            Image im = new Image(in);
                            originalFrame.setImage(im);
                        });

                        Thread.sleep(50);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }).start();
    }

    protected void stopCamera() {
        isCameraActive = false;
        cameraCapture.release(); // Giải phóng tài nguyên camera
        lblnumber.setText("Chua bat cam");
        Platform.runLater(() -> originalFrame.setImage(null));
    }

    protected CascadeClassifier loadCascade(String source) {
        CascadeClassifier faceDetector = new CascadeClassifier(source);
        if (faceDetector.empty()) {
            System.out.println("Không thể tải bộ phân loại");
        }
        return faceDetector;
    }

    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
}
