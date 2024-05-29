package com.example.demo4;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static org.opencv.objdetect.Objdetect.CASCADE_SCALE_IMAGE;

public class MainController extends Utils {

    private Stage stage;
    @FXML
    private Button cameraButton;
    @FXML
    private ImageView originalFrame;
    @FXML
    private StackPane stackPane;
    @FXML
    private Label lblnumber ;

    String source = "src/main/resources/com/example/demo4/haarcascades/haarcascade_frontalface_alt.xml";
    CascadeClassifier faceDetector = new CascadeClassifier(source);

    private boolean isCameraActive = false;
    private VideoCapture cameraCapture;
    @FXML
    private void startStopCamera(ActionEvent event) {
        if (!isCameraActive) {
            startCamera();
            cameraButton.setText("Stop");
        } else {
            stopCamera();
            cameraButton.setText("Start");
            lblnumber.setText("Person Number");
        }
    }

    private void startCamera() {
         cameraCapture = new VideoCapture(0);
        MatOfRect rostros = new MatOfRect();
        Mat frame = new Mat();
        Mat frame_gray = new Mat();
        BufferedImage buff = null;

        isCameraActive = true;

        new Thread(() -> {
            while (cameraCapture.read(frame)) {
                if (frame.empty()) {
                    System.out.println("No detection");
                    break;
                } else {
                    try {
                        Imgproc.cvtColor(frame, frame_gray, Imgproc.COLOR_BGR2GRAY);
                        Imgproc.equalizeHist(frame_gray, frame_gray);
                        double w = frame.width();
                        double h = frame.height();
                        faceDetector.detectMultiScale(frame_gray, rostros, 1.1, 2, 0 | CASCADE_SCALE_IMAGE, new Size(30, 30), new Size(w, h));
                        Rect[] facesArray = rostros.toArray();
                        System.out.println("Số người có trong Camera: " + facesArray.length);

                        for (int i = 0; i < facesArray.length; i++) {
                            Point center = new Point((facesArray[i].x + facesArray[i].width * 0.5),
                                    (facesArray[i].y + facesArray[i].height * 0.5));
                            Imgproc.ellipse(frame,
                                    center,
                                    new Size(facesArray[i].width * 0.5, facesArray[i].height * 0.5),
                                    0,
                                    0,
                                    360,
                                    new Scalar(255, 0, 255), 4, 8, 0);

                            Mat faceROI = frame_gray.submat(facesArray[i]);
                            Imgproc.rectangle(frame,
                                    new Point(facesArray[i].x, facesArray[i].y),
                                    new Point(facesArray[i].x + facesArray[i].width, facesArray[i].y + facesArray[i].height),
                                    new Scalar(123, 213, 23, 220));
                            Imgproc.putText(frame, "this is person ",
                                    new Point(facesArray[i].x, facesArray[i].y - 20), 1, 1, new Scalar(255, 255, 255));
                        }

                        Platform.runLater(() -> {
                            int no = facesArray.length;
                            lblnumber.setText("Have" + String.valueOf(no));

                            MatOfByte mem = new MatOfByte();
                            Imgcodecs.imencode(".bmp", frame, mem);
                            InputStream in = new ByteArrayInputStream(mem.toArray());
                            Image im = new Image(in);
                            originalFrame.setImage(im);
                        });
                        Thread.sleep(50);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void stopCamera() {
        isCameraActive = false;
        cameraCapture.release(); // Giải phóng tài nguyên camera
        lblnumber.setText("Chua bat cam");
        // Set originalFrame thành hình ảnh trống
        Platform.runLater(() -> {
            originalFrame.setImage(null);
        });
    }

    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
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
                Files.copy(Paths.get(imagePath), copiedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
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
        mat.get(0, 0, ((DataBufferByte) image.getRaster().getDataBuffer()).getData());
        return image;
    }
    @FXML
    public void clickFilter(ActionEvent event) throws IOException {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("filter-view.fxml"));
            Parent root = loader.load();

            //add controller
            FilterController controller = loader.getController();
            //set thêm ảnh

            Scene newScene = new Scene(root);
            stage.setScene(newScene);
            stage.setTitle("Select Filter");
            //thêm css mới cho scene này
            newScene.getStylesheets().add(getClass().getResource("filter.css").toExternalForm());

            //controller.setImage(lastImage);
            controller.setStage(stage);
            stage.show();;
    }
}
