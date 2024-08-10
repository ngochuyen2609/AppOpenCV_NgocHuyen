package com.example.demo4;
import javafx.animation.FadeTransition;
import javafx.application.Platform;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.util.Duration;
import org.opencv.core.Mat;

import org.opencv.core.Rect;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;


public class FilterStageController extends FilterController {
    @FXML
    private ImageView CameraFrame;
    @FXML
    private AnchorPane anchorPane;
    protected VideoCapture cameraCapture;
    protected AtomicBoolean isCameraActive = new AtomicBoolean(false);

    @FXML
    protected void startStopCamera() {
        super.setUp();
        if (isCameraActive.get()) stopCamera();
        else startCamera();
    }

    protected void startCamera() {
        cameraCapture = new VideoCapture(0);

        final Mat[] frame = {new Mat()};

        CameraFrame.setFitWidth(600);
        CameraFrame.setFitHeight(450);
        isCameraActive.set(true);

        new Thread(() -> {
            while (isCameraActive.get() && cameraCapture.read(frame[0])) {
                if (frame[0].empty()) {
                    System.out.println("No detection");
                    break;
                } else {
                    try {
                        Core.flip(frame[0], frame[0], 1);
                        int val = super.getFilterType();
                        if(val > 0){
                            FaceDetector faceDetector = new FaceDetector();
                            List<Rect> facesArray = faceDetector.detectFaces(frame[0]);
                            Filter filter = new Filter();
//                            System.out.println(val);
                            for (Rect face : facesArray)
                                frame[0] = filter.overlayImage(frame[0].clone(),val, face);
                        }
                        Platform.runLater(() -> {
//                            lblnumber.setText("Have " + facesArray.size());
                            MatOfByte mem = new MatOfByte();
                            Imgcodecs.imencode(".bmp", frame[0], mem);
                            InputStream in = new ByteArrayInputStream(mem.toArray());
                            Image im = new Image(in);
                            CameraFrame.setImage(im);
                        });

                        Thread.sleep(50);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
//            stopCamera();
        }).start();
    }

    protected void stopCamera() {
        isCameraActive.set(false);
        if (cameraCapture != null) {
            cameraCapture.release(); // Giải phóng tài nguyên camera
        }
        Platform.runLater(() -> CameraFrame.setImage(null));
    }
    @FXML
    public void clickCapture(ActionEvent event) {
        Image capturedImage = captureImage();
        createFlashEffect();

        if (capturedImage == null) {
            showAlert("No Image", "Failed to capture image.");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Save Captured Image");
        dialog.setHeaderText("Enter a name for the captured image:");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Image Name");

        grid.add(new Label("Name: "), 0, 0);
        grid.add(nameField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        ButtonType submitButton = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButton, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButton) {
                return nameField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> {
            if (name.trim().isEmpty()) {
                super.showAlert("Invalid Name", "Image name cannot be empty.");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Image");
            fileChooser.setInitialFileName(name + ".png");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PNG Files", "*.png"),
                    new FileChooser.ExtensionFilter("JPG Files", "*.jpg"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );

            File file = fileChooser.showSaveDialog(new Stage());

            if (file != null) {
                try {
                    BufferedImage bImage = SwingFXUtils.fromFXImage(capturedImage, null);
                    String fileName = file.getName().toLowerCase();
                    if (fileName.endsWith(".png")) {
                        ImageIO.write(bImage, "png", file);
                    } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                        ImageIO.write(bImage, "jpg", file);
                    } else {
                        ImageIO.write(bImage, "png", file); // Mặc định lưu file dạng png
                    }
                    System.out.println("Image saved to: " + file.getAbsolutePath());

                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to save image: " + e.getMessage());
                }
            } else {
                System.out.println("File save dialog was cancelled.");
            }
        });
    }

    private void createFlashEffect() {
        // Tạo một Rectangle màu trắng trên StackPane để tạo hiệu ứng flash
        javafx.scene.shape.Rectangle flash = new javafx.scene.shape.Rectangle(anchorPane.getWidth(), anchorPane.getHeight(), javafx.scene.paint.Color.WHITE);
        anchorPane.getChildren().add(flash);

        // Tạo hiệu ứng mờ dần cho flash
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), flash);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.setOnFinished(e -> anchorPane.getChildren().remove(flash));
        fadeTransition.play();
    }

    private Image captureImage() {
        // Thực hiện chụp ảnh từ originalFrame hoặc nguồn hình ảnh của bạn
        // Đảm bảo originalFrame là kiểu phù hợp
        if (CameraFrame instanceof ImageView) {
            return ((ImageView) CameraFrame).getImage();
        } else {
            System.out.println("No image available to capture.");
            return null;
        }

    }

    @FXML
    protected void back(ActionEvent event) {
        super.back(event);
    }

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

}