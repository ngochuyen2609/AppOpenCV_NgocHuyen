package com.example.demo4;

import javafx.animation.FadeTransition;
import javafx.application.Platform;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
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


public class CameraStageController extends Controller{
    @FXML
    private Label lblnumber;
    @FXML
    private ImageView cameraFrame;
    @FXML
    private Pane anchorPane;
    @FXML
    private Pane stackPane;
    protected AtomicBoolean isCameraActive = new AtomicBoolean(false);
    protected VideoCapture cameraCapture;

    @FXML
    protected void startStopCamera() {
        if (isCameraActive.get()) {
            stopCamera();
            //lblnumber.setText("Person Number");
        } else {
//            if (isFilterValue.get()) {
//                startCameraWithFilter();
//            } else {
            startCamera();
            // }

        }
    }

    protected void startCamera() {
        cameraCapture = new VideoCapture(0);
        Mat frame = new Mat();
        anchorPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            cameraFrame.setFitWidth(newVal.doubleValue());
        });
        anchorPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            cameraFrame.setFitHeight(newVal.doubleValue());
        });

        isCameraActive.set(true);

        new Thread(() -> {
            while (isCameraActive.get() && cameraCapture.read(frame)) {
                if (frame.empty()) {
                    System.out.println("No detection");
                    break;
                } else {
                    try {
                        Core.flip(frame, frame, 1);
                        FaceDetector faceDetector = new FaceDetector();
                        List<Rect> facesArray = faceDetector.detectFaces(frame);
                        for (Rect face : facesArray) {
                            Imgproc.rectangle(frame, new Point(face.x, face.y), new Point(face.x + face.width, face.y + face.height), new Scalar(123, 213, 23, 220), 2);
                            Imgproc.putText(frame, "This is a person ", new Point(face.x, face.y - 20), 1, 1, new Scalar(255, 255, 255));
                        }
                        Platform.runLater(() -> {
                            lblnumber.setText("Have " + facesArray.size());
                            MatOfByte mem = new MatOfByte();
                            Imgcodecs.imencode(".bmp", frame, mem);
                            InputStream in = new ByteArrayInputStream(mem.toArray());
                            Image im = new Image(in);

                            cameraFrame.setImage(im);
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
        isCameraActive.set(false);
        if (cameraCapture != null) {
            cameraCapture.release(); // Giải phóng tài nguyên camera
        }
        Platform.runLater(() -> cameraFrame.setImage(null));
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
                showAlert("Invalid Name", "Image name cannot be empty.");
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
        javafx.scene.shape.Rectangle flash = new javafx.scene.shape.Rectangle(stackPane.getWidth(), stackPane.getHeight(), javafx.scene.paint.Color.WHITE);
        stackPane.getChildren().add(flash);

        // Tạo hiệu ứng mờ dần cho flash
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), flash);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.setOnFinished(e -> stackPane.getChildren().remove(flash));
        fadeTransition.play();
    }

    // Hiển thị thông báo lỗi khi chưa có ảnh chụp


    private Image captureImage() {
        // Thực hiện chụp ảnh từ originalFrame hoặc nguồn hình ảnh của bạn
        // Đảm bảo originalFrame là kiểu phù hợp
        if (cameraFrame instanceof ImageView) {
            return ((ImageView) cameraFrame).getImage();
        } else {
            System.out.println("No image available to capture.");
            return null;
        }

    }

    @FXML
    public void clickFilter(ActionEvent event) throws Exception {
        stopCamera();
        System.out.println("ban da chon filter");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FilterStage.fxml"));
        Parent root = fxmlLoader.load(); // Chỉ cần load một lần
        FilterStageController filterController = fxmlLoader.getController();

        Scene scene = new Scene(root);
        // Lấy Stage hiện tại và cập nhật Scene
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Choose Filter");
        stage.setScene(scene);
        filterController.startStopCamera();

    }

    @FXML
    public void clickChoose(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ImageStage.fxml"));
        Parent root = fxmlLoader.load(); // Chỉ cần load một lần
        ImageStageController imageStageController = fxmlLoader.getController();

        Scene scene = new Scene(root);

        // Lấy Stage hiện tại và cập nhật Scene
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Choose Image");
        stage.setScene(scene);
        stopCamera();
//        System.out.println("ban da chon anh");
        imageStageController.clickChoose();
    }
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

}
