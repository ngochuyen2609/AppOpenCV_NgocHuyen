package com.example.demo4;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class FilterController {

    private Stage stage;



    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void chooseFilter() {
        try {
            //Thêm fxml
            FXMLLoader loader =new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Truyền tham chiếu của stage vào MainController
            MainController controller = loader.getController();
            controller.setStage(stage);

            //thêm css
            scene.getStylesheets().add(getClass().getResource("takeAPhoto.css").toExternalForm());
            scene.getStylesheets().forEach(System.out::println);

            stage.setTitle("Test Frontend");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clickFilter1 (ActionEvent event){
        chooseFilter();
    }

    public void clickFilter2 (ActionEvent event){
        chooseFilter();
    }

    public void clickFilter3 (ActionEvent event){
        chooseFilter();
    }

    public void clickFilter4 (ActionEvent event){
        chooseFilter();
    }


}

}
