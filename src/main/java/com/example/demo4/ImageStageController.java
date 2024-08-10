package com.example.demo4;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

public class ImageStageController extends FilterController{
    @FXML
    private Label lblnumber;
    private WritableImage resultImage;
    @FXML
    private ImageView image;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private StackPane image_layout;
    public StackPane getImage_layout() {
        return image_layout;
    }
    public File copiedFile;
    public File selectedFile;
    public String imagePath;

    @FXML
    public void clickChoose() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
        this.selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null ) {
            super.setUp();
            // Lưu đường dẫn của tệp ảnh đã chọn vào biến cục bộ
            this.imagePath = selectedFile.getAbsolutePath();
            String selectedFolderPath = "selected";

            File selectedFolder = new File(selectedFolderPath);
            if (!selectedFolder.exists()) {
                selectedFolder.mkdir();
            }

            this.copiedFile = new File(selectedFolderPath, "selected_image.jpg");

            try {
                Files.copy(Paths.get(imagePath), copiedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            Mat originalImage = Imgcodecs.imread(copiedFile.getAbsolutePath());
//            originalImage = resizeImage(originalImage,600,450);
            image.setFitHeight(450);
            image.setFitWidth(600);
            image.setPreserveRatio(true);
            if (originalImage.empty()) {
                System.out.println("Không thể mở ảnh: " + copiedFile.getAbsolutePath());
                return;
            }

            Mat newImage = originalImage;
            FaceDetector faceDetector = new FaceDetector();
            List<Rect> facesArray = faceDetector.detectFaces(newImage);
            for (Rect face : facesArray) {
                Imgproc.rectangle(newImage, new Point(face.x, face.y), new Point(face.x + face.width, face.y + face.height), new Scalar(123, 213, 23, 220), 2);
                Imgproc.putText(newImage, "This is a person ", new Point(face.x, face.y - 20), 1, 1, new Scalar(255, 255, 255));
            }
            lblnumber.setText("Have " + facesArray.size());
            // Hiển thị ảnh đã chọn ban đầu
            BufferedImage bufferedImage = matToBufferedImage(newImage);
            resultImage = SwingFXUtils.toFXImage(bufferedImage, null);

            image.setImage(resultImage);

            ImageZoomPane imageZoomPane = new ImageZoomPane(image);
            this.anchorPane.getChildren().add(imageZoomPane);
        }
    }
    @FXML
    public void setImage(ActionEvent event){
        int val = super.getFilterType();
        Mat originalImage = Imgcodecs.imread(copiedFile.getAbsolutePath());
        if (originalImage.empty()) {
            System.out.println("Không thể mở ảnh: " + copiedFile.getAbsolutePath());
            return;
        }
        Mat newImage = originalImage;
        FaceDetector faceDetector = new FaceDetector();
        List<Rect> facesArray = faceDetector.detectFaces(newImage);
        Filter filter = new Filter();

        for (Rect face : facesArray) {
            Point center = new Point((face.x + face.width * 0.5), (face.y + face.height * 0.5));
            if(val == 0){
                Imgproc.rectangle(newImage, new Point(face.x, face.y), new Point(face.x + face.width, face.y + face.height), new Scalar(123, 213, 23, 220), 2);
//                Imgproc.putText(newImage, "This is person ", new Point(face.x, face.y - 20), 1, 1, new Scalar(255, 255, 255));
            }
            else newImage = filter.overlayImage(newImage.clone(),val,face);
        }
        // Hiển thị ảnh đã chọn ban đầu
        BufferedImage bufferedImage = matToBufferedImage(newImage);
        resultImage = SwingFXUtils.toFXImage(bufferedImage, null);
        image.setImage(resultImage);
    }
    @FXML
    public void clickCapture(ActionEvent event) {
        Image capturedImage = resultImage;

        if (capturedImage == null) {
            super.showAlert("No Image", "Failed to save image.");
            return;
        }

        javafx.scene.control.Dialog<String> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Save Image");
        dialog.setHeaderText("Enter a name for the new image:");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        javafx.scene.control.TextField nameField = new TextField();
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
    @Override
    public void back(ActionEvent event) {
        super.back(event);
    }
    BufferedImage matToBufferedImage(Mat mat) {
        int type = (mat.channels() == 1) ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR;
        BufferedImage image = new BufferedImage(mat.width(), mat.height(), type);
        mat.get(0, 0, ((DataBufferByte) image.getRaster().getDataBuffer()).getData());
        return image;
    }
    private Mat resizeImage(Mat image, int targetWidth, int targetHeight) {
        int width = image.width();
        int height = image.height();
        double ratio = Math.min((double) targetWidth / width, (double) targetHeight / height);
        int newWidth = (int) (width * ratio);
        int newHeight = (int) (height * ratio);

        Mat resizedImage = new Mat();
        Imgproc.resize(image, resizedImage, new org.opencv.core.Size(newWidth, newHeight));
        return resizedImage;
    }

}
