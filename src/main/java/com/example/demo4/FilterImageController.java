package com.example.demo4;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class FilterImageController extends Filter {
    private static int fillterValue = -1;
    private ChooseImage chooseImage;


    @Override
    public void chooseFilter() {
        if (chooseImage != null && chooseImage.getSelectedFile() != null) {
            Mat src = chooseImage.getOriginalImage();

            if (src.empty()) {
                System.out.println("Không thể mở ảnh");
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
                if (fillterValue == -1) {
                    Imgproc.rectangle(src, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                            new Scalar(0, 255, 0), 3);
                } else {
                    double stickerScale = 1.0;
                    int stickerWidth = (int) (rect.width * stickerScale);
                    int stickerHeight = (int) (sticker.rows() * stickerWidth / sticker.cols());

                    int stickerX = rect.x + rect.width / 2 - stickerWidth / 2;
                    int stickerY = rect.y - stickerHeight;

                    Mat resizedsticker = new Mat();
                    Imgproc.resize(sticker, resizedsticker, new Size(stickerWidth, stickerHeight));

                    src = overlayImage(src, resizedsticker, stickerX, stickerY);
                }
            }

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
                zoomableImageView.setImage(resultImage);
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
}
