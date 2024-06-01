package com.example.demo4;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.opencv.objdetect.Objdetect.CASCADE_SCALE_IMAGE;

public class StartStopCamera extends FilterController {
    @FXML
    protected ImageView originalFrame;
    @FXML
    protected Button cameraButton;
    @FXML
    protected Label lblnumber;

    protected CascadeClassifier faceDetector;
    protected AtomicBoolean isCameraActive = new AtomicBoolean(false);
    protected VideoCapture cameraCapture;
    protected Mat sticker;

    public void setSticker(Mat sticker) {
        this.sticker = sticker;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    protected AtomicBoolean isFilterValue = new AtomicBoolean(false);
    public void setIsFilterValue(boolean value){
        isFilterValue.set(value);
    }

    @FXML
    protected void startStopCamera(ActionEvent event) {
        if (isCameraActive.get()) {
            stopCamera();
            cameraButton.setText("Start");
            lblnumber.setText("Person Number");
        } else {
            if (isFilterValue.get()) {
                startCameraWithFilter();
            } else {
                startCamera();
            }
            cameraButton.setText("Stop");
        }
    }

    protected void startCamera() {
        cameraCapture = new VideoCapture(0);
        Mat frame = new Mat();

        isCameraActive.set(true);

        new Thread(() -> {
            while (isCameraActive.get() && cameraCapture.read(frame)) {
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

    protected CascadeClassifier faceCascade = loadCascade("src/main/resources/com/example/demo4/haarcascades/haarcascade_frontalface_default.xml");

    protected void startCameraWithFilter() {
        cameraCapture = new VideoCapture(0);
        final Mat[] frame = {new Mat()};
        Mat grayFrame = new Mat();

        isCameraActive.set(true);

        new Thread(() -> {
            while (isCameraActive.get() && cameraCapture.read(frame[0])) {
                if (frame[0].empty()) {
                    System.out.println("No detection");
                    break;
                } else {
                    Core.flip(frame[0], frame[0], 1);
                    Imgproc.cvtColor(frame[0], grayFrame, Imgproc.COLOR_BGR2GRAY);
                    Imgproc.equalizeHist(grayFrame, grayFrame);

                    MatOfRect faces = new MatOfRect();
                    faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, CASCADE_SCALE_IMAGE, new Size(30, 30), new Size());

                    // Add sticker to each face
                    if (sticker != null) {
                        for (Rect rect : faces.toArray()) {
                            double stickerScale = 0.9;
                            int stickerWidth = (int) (rect.width * stickerScale);
                            int stickerHeight = (int) (sticker.rows() * stickerWidth / sticker.cols());

                            int stickerX = rect.x + rect.width / 2 - stickerWidth / 2;
                            int stickerY = rect.y - stickerHeight;

                            Mat resizedSticker = new Mat();
                            Imgproc.resize(sticker, resizedSticker, new Size(stickerWidth, stickerHeight));

                            frame[0] = overlayImage(frame[0], resizedSticker, stickerX, stickerY);
                        }
                    }

                    // Update ImageView with the processed frame
                    Platform.runLater(() -> {
                        if (!frame[0].empty()) {
                            MatOfByte mem = new MatOfByte();
                            Imgcodecs.imencode(".bmp", frame[0], mem);
                            InputStream in = new ByteArrayInputStream(mem.toArray());
                            Image im = new Image(in);
                            originalFrame.setImage(im);
                        }
                    });

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }

        }).start();
    }


    protected void stopCamera() {
        isCameraActive.set(false);
        if (cameraCapture != null) {
            cameraCapture.release(); // Giải phóng tài nguyên camera
        }
        Platform.runLater(() -> originalFrame.setImage(null));
    }

    protected CascadeClassifier loadCascade(String source) {
        CascadeClassifier faceDetector = new CascadeClassifier(source);
        if (faceDetector.empty()) {
            System.out.println("Không thể tải bộ phân loại");
        }
        return faceDetector;
    }

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
}
