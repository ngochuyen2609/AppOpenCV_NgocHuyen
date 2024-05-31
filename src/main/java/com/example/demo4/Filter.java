package com.example.demo4;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public abstract class Filter {
    protected Stage stage;
    @FXML
    protected ImageView filter1;
    @FXML
    protected ImageView filter2;
    @FXML
    protected ImageView filter3;
    @FXML
    protected ImageView filter4;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public abstract void chooseFilter() ;

    public void clickFilter1(ActionEvent event) {
        chooseFilter();
    }

    public void clickFilter2(ActionEvent event) {
        chooseFilter();
    }

    public void clickFilter3(ActionEvent event) {
        chooseFilter();
    }

    public void clickFilter4(ActionEvent event) {
        chooseFilter();
    }
}