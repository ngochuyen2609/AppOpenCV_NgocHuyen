package com.example.demo4;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.awt.image.BufferedImage;
import java.io.IOException;
public class FilterImageController extends Filter {
    private static int fillterValue = -1;
    @FXML
    private ImageView image;
    @FXML
    private StackPane image_layout;
    public StackPane getImage_layout() {
        return image_layout;
    }

    public ImageView getImage() {
        return image;
    }
    @Override
    public void chooseFilter() {
        Mat src = Imgcodecs.imread("selected/selected_image.jpg");
        if (src.empty()) {
            System.out.println("Không thể mở ảnh: " + "selected_image.jpg");
            return;
        }

        CascadeClassifier faceDetector = new CascadeClassifier("src/main/resources/com/example/demo4/lbpcascades/lbpcascade_frontalface_improved.xml");
        if (faceDetector.empty()) {
            System.out.println("Không thể tải bộ phân loại");
            return;
        }

        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(src, faceDetections);

        System.out.println("Số lượng khuôn mặt phát hiện: " + faceDetections.toArray().length);
        for (Rect rect : faceDetections.toArray()) {
//            if (fillterValue == -1) {
//                Imgproc.rectangle(src, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
//                        new Scalar(0, 255, 0), 3);
//            }
            double stickerScale = 1.0;
            int stickerWidth = (int) (rect.width * stickerScale);
            int stickerHeight = (int) (sticker.rows() * stickerWidth / sticker.cols());

            int stickerX = rect.x + rect.width / 2 - stickerWidth / 2;
            int stickerY = rect.y - stickerHeight;

            Mat resizedSticker = new Mat();
            Imgproc.resize(sticker, resizedSticker, new Size(stickerWidth, stickerHeight));

            src = overlayImage(src, resizedSticker, stickerX, stickerY);

        }

        BufferedImage bufferedImage = matToBufferedImage(src);
        WritableImage resultImage = SwingFXUtils.toFXImage(bufferedImage, null);

        ZoomableImageView zoomableImageView = new ZoomableImageView();
        zoomableImageView.setImage(resultImage);
        zoomableImageView.fitWidthProperty().bind(this.getImage_layout().widthProperty());
        zoomableImageView.fitHeightProperty().bind(this.getImage_layout().heightProperty());
        zoomableImageView.setPreserveRatio(true);

;       this.getImage_layout().getChildren().add(zoomableImageView);
    }

    @Override
    public void back(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("image-view.fxml"));
            Parent root = loader.load();

            ImageViewController controller = loader.getController();
            controller.setStage(stage);

            Scene scene = new Scene(root);

            // Thêm CSS
            scene.getStylesheets().add(getClass().getResource("image.css").toExternalForm());
            String selectedFolderPath = "selected";
            String outputImagePath = selectedFolderPath + "/output.jpg";

            Mat src = Imgcodecs.imread("selected/selected_image.jpg");

            if (src.empty()) {
                System.out.println("Không thể mở ảnh: " + "selected_image.jpg");
                return;
            }

            CascadeClassifier faceDetector = new CascadeClassifier("src/main/resources/com/example/demo4/lbpcascades/lbpcascade_frontalface_improved.xml");

            if (faceDetector.empty()) {
                System.out.println("Không thể tải bộ phân loại");
                return;
            }

            MatOfRect faceDetections = new MatOfRect();
            faceDetector.detectMultiScale(src, faceDetections);
            Imgcodecs.imwrite(outputImagePath, src);

            BufferedImage bufferedImage = matToBufferedImage(src);
            WritableImage resultImage = SwingFXUtils.toFXImage(bufferedImage, null);
            controller.displayImage(resultImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void done(ActionEvent event){
        Mat src = Imgcodecs.imread("selected/selected_image.jpg");
        if (src.empty()) {
            System.out.println("Không thể mở ảnh: " + "selected_image.jpg");
            return;
        }

        CascadeClassifier faceDetector = new CascadeClassifier("src/main/resources/com/example/demo4/lbpcascades/lbpcascade_frontalface_improved.xml");
        if (faceDetector.empty()) {
            System.out.println("Không thể tải bộ phân loại");
            return;
        }

        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(src, faceDetections);

        System.out.println("Số lượng khuôn mặt phát hiện: " + faceDetections.toArray().length);
        for (Rect rect : faceDetections.toArray()) {
//            if (fillterValue == -1) {
//                Imgproc.rectangle(src, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
//                        new Scalar(0, 255, 0), 3);
//            }
            double stickerScale = 1.0;
            int stickerWidth = (int) (rect.width * stickerScale);
            int stickerHeight = (int) (sticker.rows() * stickerWidth / sticker.cols());

            int stickerX = rect.x + rect.width / 2 - stickerWidth / 2;
            int stickerY = rect.y - stickerHeight;

            Mat resizedSticker = new Mat();
            Imgproc.resize(sticker, resizedSticker, new Size(stickerWidth, stickerHeight));

            src = overlayImage(src, resizedSticker, stickerX, stickerY);

        }

        BufferedImage bufferedImage = matToBufferedImage(src);
        WritableImage resultImage = SwingFXUtils.toFXImage(bufferedImage, null);
        ChooseImage chooseImage = new ChooseImage();
        chooseImage.setStage(stage);
        chooseImage.displayImage(resultImage);
    }
}