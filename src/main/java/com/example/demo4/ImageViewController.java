package com.example.demo4;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

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
    public void chooseImage(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");

        //Chỉ chọn ảnh kiểu .png .jpg .gif
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());

            // Hiển thị ảnh đã chọn trong một stage mới
            try {
                this.showImageInCurrentStage(selectedFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
