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
            if (fillterValue == -1) {
                Imgproc.rectangle(src, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(0, 255, 0), 3);
            }
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

        chooseImage.displayImage(resultImage);
    }
}