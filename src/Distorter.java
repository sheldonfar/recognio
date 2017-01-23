import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Random;

class Distorter {
    private static final Random random = new Random();

    private static Mat blur(Mat img) {
        int w = 2 * (int) (random.nextFloat() * ((21) / 3)) + 11;
        int h = 2 * (int) (random.nextFloat() * ((21) / 3)) + 11;
        int s = random.nextInt(10) + 1;
        Imgproc.GaussianBlur(img, img, new Size(w, h), s);
        return img;
    }

    private static Mat crop(Mat img) {
        int x = random.nextInt(img.width() / 2) + 1;
        int y = random.nextInt(img.height() / 2) + 1;
        int w = img.width() - x;
        int h = img.height() - y;
        Rect roi = new Rect(x, y, w, h);
        return new Mat(img, roi);
    }

    private static Mat rotate(Mat img) {
        Point center = new Point(img.cols() / 2, img.rows() / 2);
        int degrees = random.nextInt(60) + 1;
        Mat rotImage = Imgproc.getRotationMatrix2D(center, degrees, 1.0);
        Mat newImg = new Mat();
        Imgproc.warpAffine(img, newImg, rotImage, newImg.size());
        return newImg;
    }

    private static Mat contrast(Mat img) {
        Mat dst = new Mat(img.rows(), img.cols(), img.type());
        int alpha = random.nextInt(3) + 1;
        img.convertTo(dst, -1, alpha, 0);
        return dst;
    }

    static Mat distortImage(Mat img) {
        int choice = random.nextInt(4) + 1;
        Mat clone = img.clone();
        switch (choice) {
            case 1:
                return blur(clone);
            case 2:
                return crop(clone);
            case 3:
                return rotate(clone);
            case 4:
                return contrast(clone);
            default:
                return clone;
        }
    }
}
