package com.example.demo4;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class FilterController extends Controller{
    @FXML
    protected Button cameraButton;
    @FXML
    protected ToggleGroup filter;
    @FXML
    protected RadioButton filter_button0;
    @FXML
    protected RadioButton filter_button1;
    @FXML
    protected RadioButton filter_button2;
    @FXML
    protected RadioButton filter_button3;
    @FXML
    protected RadioButton filter_button4;
    @FXML
    protected RadioButton filter_button5;
    @FXML
    protected ImageView filter_image0;
    @FXML
    protected ImageView filter_image1;
    @FXML
    protected ImageView filter_image2;
    @FXML
    protected ImageView filter_image3;
    @FXML
    protected ImageView filter_image4;
    @FXML
    protected ImageView filter_image5;

    protected void setUp(){
        filter_image0.setOnMouseClicked(event -> {filter_button0.fire(); });
        filter_image1.setOnMouseClicked(event -> {filter_button1.fire(); });
        filter_image2.setOnMouseClicked(event -> {filter_button2.fire(); });
        filter_image3.setOnMouseClicked(event -> {filter_button3.fire(); });
        filter_image4.setOnMouseClicked(event -> {filter_button4.fire(); });
        filter_image5.setOnMouseClicked(event -> {filter_button5.fire(); });
//        filter_image6.setOnMouseClicked(event -> {filter_button6.fire(); });
//        System.out.println("set up ed");
    }
    protected int filterType = 0;
    protected int getFilterType(){
        if(filter_button1.isSelected()) filterType = 1;
        else if(filter_button2.isSelected()) filterType = 2;
        else if(filter_button3.isSelected()) filterType = 3;
        else if(filter_button4.isSelected()) filterType = 4;
        else if(filter_button5.isSelected()) filterType = 5;
        else filterType = 0;
        return filterType;
    }
}
