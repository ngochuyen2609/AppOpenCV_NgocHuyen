package com.example.demo4;

import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class ZoomableImageView extends ImageView {
    private double scale = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    private double initialX = 0;
    private double initialY = 0;

    public ZoomableImageView() {
        setPreserveRatio(true);
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

        offsetX = mouseX - (mouseX - offsetX) * scaleChange;
        offsetY = mouseY - (mouseY - offsetY) * scaleChange;

        updateImageView();
    }

    private void handleMousePressed(MouseEvent event) {
        initialX = event.getX() - offsetX;
        initialY = event.getY() - offsetY;
    }

    private void handleMouseDragged(MouseEvent event) {
        offsetX = event.getX() - initialX;
        offsetY = event.getY() - initialY;

        // Ensure the image does not go out of bounds
        double imageWidth = getImage().getWidth() * scale;
        double imageHeight = getImage().getHeight() * scale;
        double paneWidth = getParent().getBoundsInLocal().getWidth();
        double paneHeight = getParent().getBoundsInLocal().getHeight();

        offsetX = Math.max(offsetX, paneWidth - imageWidth);
        offsetX = Math.min(offsetX, 0);
        offsetY = Math.max(offsetY, paneHeight - imageHeight);
        offsetY = Math.min(offsetY, 0);

        updateImageView();
    }

    private void updateImageView() {
        this.setScaleX(scale);
        this.setScaleY(scale);
        this.setTranslateX(offsetX);
        this.setTranslateY(offsetY);
    }
}
