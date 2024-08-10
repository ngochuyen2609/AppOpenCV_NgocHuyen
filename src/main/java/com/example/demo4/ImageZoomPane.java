package com.example.demo4;

import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ScrollPane;

import static java.lang.Double.max;
import static java.lang.Math.min;

//import static com.sun.webkit.graphics.WCImage.getImage;

public class ImageZoomPane extends StackPane {

    double scaleFactor = 1;
    private static final double MAX_SCALE = 10.0;
    private static final double MIN_SCALE = 1;
    private ImageView imageView;
    private ScrollPane scrollPane;
    private double offsetX = 0;
    private double offsetY = 0;
    private double initialX = 0;
    private double initialY = 0;

    public ImageZoomPane(ImageView imageView) {
        this.imageView = imageView;
        this.scrollPane = new ScrollPane(imageView);
        this.scrollPane.setPannable(false);
        this.scrollPane.setFitToWidth(true);
        this.scrollPane.setFitToHeight(true);

        this.getChildren().add(scrollPane);

        // Đặt sự kiện cuộn chuột cho ImageView
        imageView.setOnScroll(this::handleScroll);

        // Đặt sự kiện kéo chuột cho ImageView
        imageView.setOnMousePressed(this::handleMousePressed);
        imageView.setOnMouseDragged(this::handleMouseDragged);
    }

    private void handleScroll(ScrollEvent event) {
        if (event.getDeltaY() == 0) return;

        scaleFactor = (event.getDeltaY() > 0) ? 1.1 : 0.9;

        double currentScale = imageView.getScaleX();
        double newScale = currentScale * scaleFactor;

        // Đảm bảo scale nằm trong giới hạn
        if (newScale > MAX_SCALE) {
            newScale = MAX_SCALE;
        } else if (newScale < MIN_SCALE) {
            newScale = MIN_SCALE;
        }

        // Lấy tọa độ của con trỏ chuột trên hình ảnh
        double mouseX = event.getX();
        double mouseY = event.getY();

        /// Lấy tọa độ của con trỏ chuột trong hệ toạ độ của ImageView
        double viewportOffsetX = scrollPane.getHvalue() * (imageView.getBoundsInParent().getWidth() - scrollPane.getViewportBounds().getWidth());
        double viewportOffsetY = scrollPane.getVvalue() * (imageView.getBoundsInParent().getHeight() - scrollPane.getViewportBounds().getHeight());

        double relativeMouseX = (mouseX + viewportOffsetX) / currentScale;
        double relativeMouseY = (mouseY + viewportOffsetY) / currentScale;

        imageView.setScaleX(newScale);
        imageView.setScaleY(newScale);

        // Đặt lại vị trí cuộn của ScrollPane để giữ con trỏ chuột ở vị trí cũ trên hình ảnh
//        scrollPane.layout();

        double newViewportOffsetX = relativeMouseX - mouseX;
        double newViewportOffsetY = relativeMouseY - mouseY;
        scrollPane.setHvalue(newViewportOffsetX / (imageView.getBoundsInParent().getWidth() - scrollPane.getViewportBounds().getWidth()));
        scrollPane.setVvalue(newViewportOffsetY / (imageView.getBoundsInParent().getHeight() - scrollPane.getViewportBounds().getHeight()));

        scaleFactor = newScale;
        event.consume();  // Ngăn chặn sự kiện tiếp tục lan truyền
    }

    private void handleMousePressed(MouseEvent event) {
        initialX = event.getX() - offsetX;
        initialY = event.getY() - offsetY;
    }

    private void handleMouseDragged(MouseEvent event) {
        offsetX = event.getX() - initialX;
        offsetY = event.getY() - initialY;
        // Ensure the image does not go out of bounds
//        double imageWidth = imageView.getFitWidth();
//        double imageHeight = imageView.getFitHeight();
//        double paneWidth = getParent().getLayoutX();
//        double paneHeight = getParent().getLayoutY();
//
//        offsetX = paneWidth - imageWidth;
//        offsetY = paneHeight - imageHeight;

        imageView.setTranslateX(offsetX);
        imageView.setTranslateY(offsetY);
//        updateImageView();
        event.consume();

        initialX = event.getX() - offsetX;
        initialY = event.getY() - offsetY;
    }
}


