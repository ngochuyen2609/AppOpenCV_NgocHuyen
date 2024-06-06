package com.example.demo4;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.opencv.objdetect.Objdetect.CASCADE_SCALE_IMAGE;

public class FilterController extends Filter {
    @Override
    public void back(ActionEvent event){ // reset not with Filter
        try {
            // Thêm fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Truyền tham chiếu của stage vào MainController
            MainController controller = loader.getController();
            controller.setStage(stage);  // Pass the stage to the MainController
            controller.startStopCamera(new ActionEvent());
            // Thêm css
            scene.getStylesheets().add(getClass().getResource("takeAPhoto.css").toExternalForm());

            stage.setTitle("Test Frontend");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected VideoCapture cameraCapture;
    protected AtomicBoolean isCameraActive = new AtomicBoolean(false);
    protected CascadeClassifier faceCascade = loadCascade("src/main/resources/com/example/demo4/haarcascades/haarcascade_frontalface_default.xml");

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

    @FXML
    protected ImageView originalFrame;

    @Override
    public void chooseFilter() {
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


                    // Chuyển đổi khung hình thành hình vuông với góc bo tròn
                    frame[0] = convertToSquareWithRoundedCorners(frame[0]);

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

    protected Mat convertToSquareWithRoundedCorners(Mat frame) {
        // Chuyển đổi khung hình thành hình vuông
        int size = Math.min(frame.width(), frame.height());
        int x = (frame.width() - size) / 2;
        int y = (frame.height() - size) / 2;
        Rect roi = new Rect(x, y, size, size);
        Mat squareFrame = new Mat(frame, roi);

        // Tạo mặt nạ với các góc bo tròn
        Mat mask = Mat.zeros(size, size, CvType.CV_8UC1);
        int radius = size / 6; // Điều chỉnh bán kính nếu cần
        Imgproc.rectangle(mask, new Point(radius, 0), new Point(size - radius, size), new Scalar(255), -1);
        Imgproc.rectangle(mask, new Point(0, radius), new Point(size, size - radius), new Scalar(255), -1);
        Imgproc.circle(mask, new Point(radius, radius), radius, new Scalar(255), -1);
        Imgproc.circle(mask, new Point(size - radius, radius), radius, new Scalar(255), -1);
        Imgproc.circle(mask, new Point(radius, size - radius), radius, new Scalar(255), -1);
        Imgproc.circle(mask, new Point(size - radius, size - radius), radius, new Scalar(255), -1);

        // Áp dụng mặt nạ vào khung hình và tạo khung hình vuông với các góc bo tròn
        Mat maskedFrame = new Mat();
        squareFrame.copyTo(maskedFrame, mask);

        // Tạo khung nền để chỉ hiển thị phần khung hình vuông với các góc bo tròn
        Mat result = Mat.zeros(size, size, squareFrame.type());
        maskedFrame.copyTo(result, mask);

        return result;
    }

    @Override
    public void done (ActionEvent event){
        try {
            // Thêm fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Truyền tham chiếu của stage vào MainController
            MainController controller = loader.getController();
            controller.setStage(stage);  // Pass the stage to the MainController
            controller.setSticker(sticker);
            controller.setIsFilterValue(true);
            controller.startStopCamera(new ActionEvent());
            // Thêm css
            scene.getStylesheets().add(getClass().getResource("takeAPhoto.css").toExternalForm());

            stage.setTitle("Test Frontend");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
