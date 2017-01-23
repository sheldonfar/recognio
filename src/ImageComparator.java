import org.opencv.core.*;
import org.opencv.features2d.*;

import java.util.LinkedList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.resize;

class ImageComparator {

    static class ComparisonResult {
        MethodResult valueORB;
        MethodResult valueSURF;
        MethodResult valueSIFT;

        static class MethodResult {
            final String methodName;
            final int value;
            final long time;
            final String criterion;
            final Boolean decision;

            MethodResult(String methodName, long time, int value, String criterion, Boolean decision) {
                this.methodName = methodName;
                this.time = time;
                this.value = value;
                this.criterion = criterion;
                this.decision = decision;
            }
        }

        Boolean getEqual() {
            MethodResult[] methods = {valueORB, valueSIFT, valueSURF};
            int decision = 0;
            for (MethodResult method : methods) {
                if (method.decision) {
                    decision++;
                }
            }
            return decision > methods.length / 2;
        }

        MethodResult getValueORB() {
            return valueORB;
        }

        MethodResult getValueSURF() {
            return valueSURF;
        }

        MethodResult getValueSIFT() {
            return valueSIFT;
        }
    }

    ImageComparator() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    ComparisonResult compare(Mat img1, Mat img2) {
        Mat cImg1 = img1.clone();
        Mat cImg2 = img2.clone();
        //resizeImages(cImg1, cImg2);

        ComparisonResult cr = new ComparisonResult();
        cr.valueORB = compareORB(cImg1, cImg2);
        cr.valueSURF = compareSURF(cImg1, cImg2);
        cr.valueSIFT = compareSIFT(cImg1, cImg2);
        return cr;
    }

    private void resizeImages(Mat img1, Mat img2) {
        int width = Math.min(img1.width(), img2.width());
        int height = Math.min(img1.height(), img2.height());

        width = Math.min(width, height);
        height = width;

        resize(img1, img2, new Size(width, height));
        resize(img2, img1, new Size(width, height));
    }

    private ComparisonResult.MethodResult featureDescription(String methodName, int method, int goodMatchCount, Mat img1, Mat img2) {
        long startTime = System.currentTimeMillis();

        MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
        MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
        MatOfKeyPoint descriptors1 = new MatOfKeyPoint();
        MatOfKeyPoint descriptors2 = new MatOfKeyPoint();

        FeatureDetector detector = FeatureDetector.create(method);
        DescriptorExtractor extractor = DescriptorExtractor.create(method - 2);

        detector.detect(img1, keypoints1);
        detector.detect(img2, keypoints2);

        extractor.compute(img1, keypoints1, descriptors1);
        extractor.compute(img2, keypoints2, descriptors2);

        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);

        List<MatOfDMatch> matches = new LinkedList<>();
        matcher.knnMatch(descriptors1, descriptors2, matches, 2);

        LinkedList<DMatch> goodMatchesList = new LinkedList<>();

        for (MatOfDMatch matofDMatch : matches) {
            DMatch[] dmatcharray = matofDMatch.toArray();
            DMatch m1 = dmatcharray[0];
            DMatch m2 = dmatcharray[1];
            if (m1.distance <= m2.distance * 0.7f) {
                goodMatchesList.addLast(m1);
            }
        }

        long estimatedTime = System.currentTimeMillis() - startTime;

        return new ComparisonResult.MethodResult(methodName, estimatedTime, goodMatchesList.size(), "value " + Character.toString((char) 0x2265) + " " + goodMatchCount, goodMatchesList.size() >= goodMatchCount);
    }

    private ComparisonResult.MethodResult compareSURF(Mat img1, Mat img2) {
        return featureDescription("SURF", 4, 15, img1, img2);
    }

    private ComparisonResult.MethodResult compareSIFT(Mat img1, Mat img2) {
        return featureDescription("SIFT", 3, 10, img1, img2);
    }

    private ComparisonResult.MethodResult compareORB(Mat img1, Mat img2) {
        int retVal = 0;
        long startTime = System.currentTimeMillis();

        MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
        MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
        Mat descriptors1 = new Mat();
        Mat descriptors2 = new Mat();

        FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);

        detector.detect(img1, keypoints1);
        detector.detect(img2, keypoints2);

        extractor.compute(img1, keypoints1, descriptors1);
        extractor.compute(img2, keypoints2, descriptors2);

        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        MatOfDMatch matches = new MatOfDMatch();

        if (descriptors2.cols() == descriptors1.cols()) {
            matcher.match(descriptors1, descriptors2, matches);

            DMatch[] match = matches.toArray();
            double max_dist = 0;
            double min_dist = 100;

            for (int i = 0; i < descriptors1.rows(); i++) {
                double dist = match[i].distance;
                if (dist < min_dist) min_dist = dist;
                if (dist > max_dist) max_dist = dist;
            }

            for (int i = 0; i < descriptors1.rows(); i++) {
                if (match[i].distance <= 10) {
                    retVal++;
                }
            }
        }

        long estimatedTime = System.currentTimeMillis() - startTime;

        return new ComparisonResult.MethodResult("ORB", estimatedTime, retVal, "value > 0", retVal > 0);
    }
}
