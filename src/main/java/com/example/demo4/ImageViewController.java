package com.example.demo4;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

public class ImageViewController {

    @FXML
    private ImageView image;
    private Stage stage;
    private Image lastImage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public ImageView getImage() {
        return image;
    }


    public void setImage(Image a) {
        image.setImage(a);
        image.getStyleClass().add("image");
        this.lastImage = a; // Cập nhật ảnh gần nhất
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
                controller.setImage(resultImage);
                controller.setStage(stage);
                Scene newScene = new Scene(root);

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


    private void showImageInCurrentStage(File file) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("image-view.fxml"));
        Parent root = loader.load();

        //add controller
        ImageViewController controller = loader.getController();
        Image image_new = new Image(file.toURI().toString());


        controller.setImage(image_new);

        Scene newScene = new Scene(root);

        stage.setScene(newScene); // Thiết lập scene mới vào stage hiện tại
        stage.setTitle("Selected Image");

        controller.setStage(stage);

        //thêm css mới cho scene này
        newScene.getStylesheets().add(getClass().getResource("image.css").toExternalForm());
    }


    public void clickFilter (ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("filterImage-view.fxml"));
        Parent root = loader.load();

        //add controller
        FilterImageController controller = loader.getController();
        //set thêm ảnh

        Scene newScene = new Scene(root);
        stage.setScene(newScene);
        stage.setTitle("Select Filter");

        controller.setImage(lastImage);
        controller.setStage(stage);


        //thêm css mới cho scene này
        newScene.getStylesheets().add(getClass().getResource("filterImage.css").toExternalForm());
    }

}