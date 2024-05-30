package com.example.demo4;

import javafx.animation.FadeTransition;
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
import javafx.util.Duration;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.awt.*;
import javax.imageio.ImageIO;
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

public class MainController {

    private Stage stage;
    @FXML
    private Button cameraButton;
    @FXML
    private ImageView originalFrame;
    @FXML
    private StackPane stackPane;
    @FXML
    private Label lblnumber ;
    //String source = "C:\\opencv\\sources\\data\\haarcascades\\haarcascade_frontalface_alt.xml";
    String source = "src/main/resources/com/example/demo4/haarcascades/haarcascade_frontalface_alt.xml";
    CascadeClassifier faceDetector = new CascadeClassifier(source);
//    CascadeClassifier faceDetector = new CascadeClassifier(source);

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
    Mat frame;
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
                        this.frame = frame;
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
        // Chụp ảnh ngay khi nhấn nút
        Image capturedImage = captureImage();
        // Tạo hiệu ứng flash (nếu cần thiết)
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


    // Đảm bảo cập nhật kích thước của hiệu ứng flash khi kích thước cửa sổ thay đổi
    @FXML
    public void initialize() {
        stackPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (stackPane.getChildren().size() > 1) {
                javafx.scene.shape.Rectangle flash = (javafx.scene.shape.Rectangle) stackPane.getChildren().get(stackPane.getChildren().size() - 1);
                flash.setWidth(newVal.doubleValue());
            }
        });

        stackPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (stackPane.getChildren().size() > 1) {
                javafx.scene.shape.Rectangle flash = (javafx.scene.shape.Rectangle) stackPane.getChildren().get(stackPane.getChildren().size() - 1);
                flash.setHeight(newVal.doubleValue());
            }
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
    public void releaseResources() {
        // Thực hiện giải phóng các tài nguyên OpenCV hoặc các tài nguyên khác tại đây
        System.out.println("Releasing resources...");
        // Ví dụ: nếu bạn có một CameraCapture object để giải phóng
        // cameraCapture.release();
    }
}
