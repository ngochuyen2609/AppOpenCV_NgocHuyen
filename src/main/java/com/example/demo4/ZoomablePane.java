package com.example.demo4;

import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;

public class ZoomablePane extends StackPane {
    private ImageView imageView;
    private double scale = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    private double initialX = 0;
    private double initialY = 0;

    public ZoomablePane(ImageView imageView) {
        this.imageView = imageView;
        getChildren().add(imageView);

        setOnScroll(this::handleScroll);
        setOnMousePressed(this::handleMousePressed);
        setOnMouseDragged(this::handleMouseDragged);
    }

    private void handleScroll(ScrollEvent event) {
        double mouseX = event.getX();
        double mouseY = event.getY();

        double oldScale = scale;
        if (event.getDeltaY() < 0) {
            scale /= 1.1;
        } else {
            scale *= 1.1;
        }

        double scaleChange = scale / oldScale;

        offsetX = (offsetX - mouseX) * scaleChange + mouseX;
        offsetY = (offsetY - mouseY) * scaleChange + mouseY;

        updateImageView();
    }

    private void handleMousePressed(MouseEvent event) {
        initialX = event.getX() - offsetX;
        initialY = event.getY() - offsetY;
    }

    private void handleMouseDragged(MouseEvent event) {
        offsetX = event.getX() - initialX;
        offsetY = event.getY() - initialY;
        updateImageView();
    }

    private void updateImageView() {
        imageView.setScaleX(scale);
        imageView.setScaleY(scale);
        imageView.setTranslateX(offsetX);
        imageView.setTranslateY(offsetY);
    }

    public void zoomToFit() {
        double paneWidth = getWidth();
        double paneHeight = getHeight();
        double imgWidth = imageView.getImage().getWidth();
        double imgHeight = imageView.getImage().getHeight();

        double scaleX = paneWidth / imgWidth;
        double scaleY = paneHeight / imgHeight;

        scale = Math.min(scaleX, scaleY);

        offsetX = 0;
        offsetY = 0;

        updateImageView();
    }
}
