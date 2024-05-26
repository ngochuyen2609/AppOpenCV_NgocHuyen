package com.example.demo4;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class Controller {
    private Stage stage;
    private ImageView imageView;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    //Bắt sự kiện ấn vô nút Start
//    public void start(ActionEvent event){
//
//    }

    //Bắt sự kiện bấm vô chụp ảnh
    public void clickCapture (ActionEvent event){
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Choose a photo ");
        dialog.setHeaderText("Choose a photo from library");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20,150,10,10));

        TextField name = new TextField();
        name.setPromptText("Name Picture");

        grid.add(new Label("Name: "),0,0);
        grid.add(name,1,0);

        dialog.getDialogPane().setContent(grid);

        ButtonType buttonYes = new ButtonType("Submit",ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonYes,ButtonType.CANCEL);

        Node button = dialog.getDialogPane().lookupButton(buttonYes);
        button.setDisable(true);

        name.textProperty().addListener((observableValue, s, t1) -> {
            button.setDisable(t1.trim().isEmpty());
        });

        //sau khi submit in ra thoong tin ddeer kieerm soats loix
        dialog.setResultConverter(dialogButton->{
            if (dialogButton == buttonYes){
                return name.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name1->{
            System.out.println(name);
        });
    }

    //Bắt sự kiện n vào nút chọn ảnh
     public void clickChoose (ActionEvent event) {
         FileChooser fileChooser = new FileChooser();
         fileChooser.setTitle("Open Resource File");

         //Chỉ chọn ảnh kiểu .png .jpg .gif
         fileChooser.getExtensionFilters().addAll(
                 new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
         File selectedFile = fileChooser.showOpenDialog(stage); // Sử dụng stage từ trường stage
         if (selectedFile != null) {
             System.out.println("Selected file: " + selectedFile.getAbsolutePath());

            // Hiển thị ảnh đã chọn trong một stage mới
             try {
                 showImageInCurrentStage(selectedFile);
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
        Image image = new Image(file.toURI().toString());
        controller.setImage(image);

        Scene newScene = new Scene(root);

        stage.setScene(newScene); // Thiết lập scene mới vào stage hiện tại
        stage.setTitle("Selected Image");

        //thêm css mới cho scene này
        newScene.getStylesheets().add(getClass().getResource("image.css").toExternalForm());
    }
}