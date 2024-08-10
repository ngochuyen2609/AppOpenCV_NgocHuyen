package com.example.demo4;


import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class Filter {
    private Mat sticker;
    double[] Scale = {0,0.9,1.3,1.0,0.45,0.45,1.0};

    BufferedImage matToBufferedImage(Mat mat) {
        int type = (mat.channels() == 1) ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR;
        BufferedImage image = new BufferedImage(mat.width(), mat.height(), type);
        mat.get(0, 0, ((DataBufferByte) image.getRaster().getDataBuffer()).getData());
        return image;
    }

    public Mat overlay(Mat background, Mat foreground, int x, int y){
        x = Math.max(0, x);
        y = Math.max(0, y);
        int w = Math.min(foreground.cols(), background.cols() - x);
        int h = Math.min(foreground.rows(), background.rows() - y);

        Rect roi = new Rect(x, y, w, h);
        Mat backgroundROI = background.submat(roi);
        Mat foregroundROI = foreground.submat(new Rect(0, 0, w, h));
        List<Mat> fgChannels = new ArrayList<>();
        Core.split(foregroundROI, fgChannels);
        Mat fgAlpha = fgChannels.get(3);

        Mat maskInv = new Mat();
        Core.bitwise_not(fgAlpha, maskInv);

        Mat fgWithoutAlpha = new Mat();
        List<Mat> fgWithoutAlphaChannels = new ArrayList<>(fgChannels);
        fgWithoutAlphaChannels.remove(3);
        Core.merge(fgWithoutAlphaChannels, fgWithoutAlpha);
        Mat bgWithMask = new Mat();
        Core.bitwise_and(backgroundROI, backgroundROI, bgWithMask, maskInv);
        Core.addWeighted(fgWithoutAlpha, 1.0, bgWithMask, 1.0, 0.0, backgroundROI);

        return background.clone();
    }

    public Mat overlayImage(Mat background, int filterType, Rect rect) {
        int stickerX1, stickerY1, stickerX2, stickerY2;
        stickerX1 = stickerY1 = stickerX2 = stickerY2 = 0;
        if(filterType == 1) this.sticker = Imgcodecs.imread(getPath("/com/example/demo4/FilterList/tai_meo.png"), Imgcodecs.IMREAD_UNCHANGED);
        else if (filterType == 2) this.sticker = Imgcodecs.imread(getPath("/com/example/demo4/FilterList/vuong_niem.png"), Imgcodecs.IMREAD_UNCHANGED);
        else if (filterType == 3) this.sticker = Imgcodecs.imread(getPath("/com/example/demo4/FilterList/message_iloveoop.png"), Imgcodecs.IMREAD_UNCHANGED);
        else if (filterType == 4) this.sticker = Imgcodecs.imread(getPath("/com/example/demo4/FilterList/blush4.png"), Imgcodecs.IMREAD_UNCHANGED);
        else this.sticker = Imgcodecs.imread(getPath("/com/example/demo4/FilterList/blush5.png"), Imgcodecs.IMREAD_UNCHANGED);

//        if(filterType == 1) this.sticker = Imgcodecs.imread("D:\\fillter\\tai_meo.png", Imgcodecs.IMREAD_UNCHANGED);
//        else if (filterType == 2) this.sticker = Imgcodecs.imread("D:\\fillter\\vuong_niem.png", Imgcodecs.IMREAD_UNCHANGED);
//        else if (filterType == 3) this.sticker = Imgcodecs.imread("D:\\fillter\\message_iloveoop.png", Imgcodecs.IMREAD_UNCHANGED);
//        else if (filterType == 4) this.sticker = Imgcodecs.imread("D:\\fillter\\blush4.png", Imgcodecs.IMREAD_UNCHANGED);
//        else this.sticker = Imgcodecs.imread("D:\\fillter\\blush5.png", Imgcodecs.IMREAD_UNCHANGED);

        // Resize sticker
        double filterScale = Scale[filterType];
        int stickerWidth = (int) (rect.width * filterScale);
        int stickerHeight = (int) (sticker.rows() * stickerWidth / sticker.cols());
        if(filterType != 4 && filterType != 5) {
            stickerX1 = rect.x + rect.width / 2 - stickerWidth / 2;
            stickerY1 = rect.y - stickerHeight;
        }else{
            stickerX1 = rect.x + rect.width / 4 - stickerWidth / 2;
            stickerX2 = rect.x + 3 * rect.width / 4 - stickerWidth / 2;
            int midFaceY = rect.y + rect.height / 2;
            stickerY1 = stickerY2 = midFaceY - stickerHeight / 3;
        }
        Mat foreground = new Mat();
        Imgproc.resize(this.sticker, foreground, new Size(stickerWidth, stickerHeight));
        Mat output = overlay(background, foreground, stickerX1, stickerY1).clone();
        if(filterType == 4 || filterType == 5) output = overlay(output.clone(), foreground, stickerX2, stickerY2).clone();
        return output.clone();
    }
    private String getPath(String source){
        String absolutePath = Objects.requireNonNull(Filter.class.getResource(source)).getPath();
        absolutePath = (String) absolutePath.substring(1,absolutePath.length());
        return absolutePath;
    }

}