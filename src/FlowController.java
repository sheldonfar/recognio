import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;

class FlowController {
    private Mat originalImg;
    private final ArrayList<Mat> modifiedImgs = new ArrayList<>();
    private final ImageComparator ic = new ImageComparator();
    private final LinkedList<ImageComparator.ComparisonResult> ics = new LinkedList<>();

    private void reset() {
        while (!ics.isEmpty()) {
            ics.removeFirst();
        }
    }

    void analyze() {
        if (originalImg == null) {
            setOriginalImage(null);
        }
        if (modifiedImgs.isEmpty()) {
            for (int i = 0; i < 15; i++) {
                modifiedImgs.add(Distorter.distortImage(originalImg));
            }
        }

        reset();

        for (Mat modifiedImg : modifiedImgs) {
            ics.add(ic.compare(originalImg, modifiedImg));
        }
    }

    ImageComparator.ComparisonResult getComparisonResult(int index) {
        if (ics.size() == 0) {
            analyze();
        }
        return ics.get(index);
    }

    int totalResults() {
        return ics.size();
    }

    void setOriginalImage(String path) {
        path = path != null ? path : "./images/original.jpg";
        originalImg = Highgui.imread(path, Highgui.CV_LOAD_IMAGE_COLOR);
    }

    void addModifiedImage(String path) {
        modifiedImgs.add(Highgui.imread(path, Highgui.CV_LOAD_IMAGE_COLOR));
    }

    void clearModifiedImages() {
        modifiedImgs.clear();
    }

    BufferedImage getOriginalImage() {
        return matToBufferedImage(originalImg);
    }

    BufferedImage getModifiedImage(int index) {
        return matToBufferedImage(modifiedImgs.get(index));
    }

    private static BufferedImage matToBufferedImage(Mat matrix) {
        BufferedImage bimg = new BufferedImage(matrix.rows(), matrix.cols(), BufferedImage.TYPE_INT_RGB);
        int cols = matrix.cols();
        int rows = matrix.rows();
        int elemSize = (int) matrix.elemSize();
        byte[] data = new byte[cols * rows * elemSize];
        int type;
        matrix.get(0, 0, data);
        switch (matrix.channels()) {
            case 1:
                type = BufferedImage.TYPE_BYTE_GRAY;
                break;
            case 3:
                type = BufferedImage.TYPE_3BYTE_BGR;
                byte b;
                for (int i = 0; i < data.length; i = i + 3) {
                    b = data[i];
                    data[i] = data[i + 2];
                    data[i + 2] = b;
                }
                break;
            default:
                return null;
        }

        if (bimg.getWidth() != cols || bimg.getHeight() != rows || bimg.getType() != type) {
            bimg = new BufferedImage(cols, rows, type);
        }
        bimg.getRaster().setDataElements(0, 0, cols, rows, data);
        return bimg;
    }
}
