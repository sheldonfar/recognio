#include <stdio.h>
#include <iostream>
#include <opencv2/opencv.hpp>
#include <string>
#include <sstream>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/nonfree/features2d.hpp>

using namespace std;
using namespace cv;

int main(void) {
    Mat original = imread("original.jpg", CV_LOAD_IMAGE_GRAYSCALE);
    vector<KeyPoint> keypoints1, keypoints2;
    int minHessian = 400;
    SurfFeatureDetector detector( minHessian );
    vector<KeyPoint> keypoints_1, keypoints_2;
    detector.detect(original, keypoints_1);
    SurfDescriptorExtractor extractor;
    Mat descriptors_1, descriptors_2;
    extractor.compute(original, keypoints_1, descriptors_1);

    FlannBasedMatcher matcher;
    vector<DMatch> matches;

    stringstream filename;
    stringstream winnerfilename;

    double closest_min_dist = 100;

    for(int i = 0; i < 10; i++) {
		filename.str("");
		filename << "cat_" << i << ".jpg";
		Mat next = imread(filename.str().c_str(), CV_LOAD_IMAGE_GRAYSCALE);

        detector.detect(next, keypoints_2);
        extractor.compute(next, keypoints_2, descriptors_2);
        matcher.match(descriptors_1, descriptors_2, matches);

        double max_dist = 0; double min_dist = 100;

        for( int i = 0; i < descriptors_1.rows; i++ )
        {
            double dist = matches[i].distance;
            if( dist < min_dist ) min_dist = dist;
            if( dist > max_dist ) max_dist = dist;
        }
        if(min_dist < closest_min_dist) {
            closest_min_dist = min_dist;
            winnerfilename.str(filename.str().c_str());
        }

        printf("\nMax dist : %f \n", max_dist );
        printf("Min dist : %f \n", min_dist );
	}

    printf("\nThis was the picture: ");
    cout << winnerfilename.str().c_str();
    printf("\n");

    waitKey(0);
    return 0;
}
