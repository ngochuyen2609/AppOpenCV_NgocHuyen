package com.example.demo4;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.opencv.objdetect.Objdetect.CASCADE_SCALE_IMAGE;
public class MainController extends StartStopCamera {
    @FXML
    private StackPane stackPane;
    String source = "src/main/resources/com/example/demo4/haarcascades/haarcascade_frontalface_alt.xml";
    CascadeClassifier faceDetector = new CascadeClassifier(source);

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

    private Image captureImage() {
        // Thực hiện chụp ảnh từ originalFrame hoặc nguồn hình ảnh của bạn
        // Đảm bảo originalFrame là kiểu phù hợp
        if (originalFrame instanceof ImageView) {
            return ((ImageView) originalFrame).getImage();
        } else {
            System.out.println("No image available to capture.");
            return null;
        }

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
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void clickChoose(ActionEvent event) {
            ChooseImage chooseImage = new ChooseImage();
            chooseImage.setStage(stage); // Thiết lập stage cho đối tượng ChooseImage
            chooseImage.clickChoose();
    }

    @FXML
    public void clickFilter(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("filter-view.fxml"));
            Parent root = loader.load();

            // Kiểm tra để đảm bảo rằng controller đúng
            FilterController controller = loader.getController();

            Scene newScene = new Scene(root);
            stage.setScene(newScene);
            stage.setTitle("Select Filter");
            newScene.getStylesheets().add(getClass().getResource("filter.css").toExternalForm());

            controller.setStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void releaseResources() {
        // Thực hiện giải phóng các tài nguyên OpenCV hoặc các tài nguyên khác tại đây
        System.out.println("Releasing resources...");
        // Ví dụ: nếu bạn có một CameraCapture object để giải phóng
        // cameraCapture.release();
    }
}
