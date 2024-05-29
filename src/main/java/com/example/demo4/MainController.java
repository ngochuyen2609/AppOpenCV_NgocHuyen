package com.example.demo4;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class MainController extends FaceDetectionController {
    @FXML
    private Image image;
    private Stage stage;

    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void clickCapture(ActionEvent event) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Choose a photo");
        dialog.setHeaderText("Choose a photo from library");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField name = new TextField();
        name.setPromptText("Name Picture");

        grid.add(new Label("Name: "), 0, 0);
        grid.add(name, 1, 0);

        dialog.getDialogPane().setContent(grid);

        ButtonType buttonYes = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonYes, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonYes) {
                return name.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name1 -> {
            System.out.println("File name :" + name.getText());
        });
    }
    @FXML
    public void clickChoose(ActionEvent event) {
        chooseImage();
    }
    // Sự kiện chọn ảnh
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            String imagePath = selectedFile.getAbsolutePath();
            String selectedFolderPath = "selected";
            String outputImagePath = selectedFolderPath + "/output.jpg";

            File selectedFolder = new File(selectedFolderPath);
            if (!selectedFolder.exists()) {
                selectedFolder.mkdir();
            }

            File copiedFile = new File(selectedFolderPath, "selected_image.jpg");
            try {
                Files.copy(Paths.get(imagePath), copiedFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            Mat src = Imgcodecs.imread(copiedFile.getAbsolutePath());

            if (src.empty()) {
                System.out.println("Không thể mở ảnh: " + copiedFile.getAbsolutePath());
                return;
            }

            CascadeClassifier faceDetector = new CascadeClassifier("src/main/resources/com/example/demo4/lbpcascades/lbpcascade_frontalface_improved.xml");

            if (faceDetector.empty()) {
                System.out.println("Không thể tải bộ phân loại");
                return;
            }

            MatOfRect faceDetections = new MatOfRect();
            faceDetector.detectMultiScale(src, faceDetections);

            for (Rect rect : faceDetections.toArray()) {
                Imgproc.rectangle(src, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(0, 255, 0), 3);
            }

            Imgcodecs.imwrite(outputImagePath, src);

            BufferedImage bufferedImage = matToBufferedImage(src);
            WritableImage resultImage = SwingFXUtils.toFXImage(bufferedImage, null);



            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("image-view.fxml"));
                Parent root = loader.load();

                ImageViewController controller = loader.getController();
                controller.setStage(stage);
                Scene newScene = new Scene(root);
                controller.setLastImage(resultImage);
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
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int type = (mat.channels() == 1) ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR;
        BufferedImage image = new BufferedImage(mat.width(), mat.height(), type);
        mat.get(0, 0, ((java.awt.image.DataBufferByte) image.getRaster().getDataBuffer()).getData());
        return image;
    }

    public void clickFilter(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("filter-view.fxml"));
        Parent root = loader.load();

        FilterController controller = loader.getController();

        Scene newScene = new Scene(root);
        stage.setScene(newScene);
        stage.setTitle("Select Filter");

        controller.setStage(stage);

        newScene.getStylesheets().add(getClass().getResource("filter.css").toExternalForm());
    }
}
