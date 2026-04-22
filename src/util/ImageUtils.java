package stereovision.util;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public final class ImageUtils {
    private ImageUtils() {
    }

    public static Mat loadColor(String path) {
        return Imgcodecs.imread(path, Imgcodecs.IMREAD_COLOR);
    }

    public static Mat loadGray(String path) {
        return Imgcodecs.imread(path, Imgcodecs.IMREAD_GRAYSCALE);
    }

    public static Mat toGray(Mat image) {
        if (image.channels() == 1) {
            return image.clone();
        }

        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        return gray;
    }

    public static void validateLoaded(Mat image, String path) {
        if (image == null || image.empty()) {
            throw new IllegalArgumentException("Could not load image: " + path);
        }
    }

    public static void validateSameSize(Mat left, Mat right) {
        if (left.rows() != right.rows() || left.cols() != right.cols()) {
            throw new IllegalArgumentException("Stereo images must have the same size.");
        }
    }

    public static Mat disparity16SToFloat(Mat disparity16S) {
        Mat disparityFloat = new Mat();
        disparity16S.convertTo(disparityFloat, CvType.CV_32F, 1.0 / 16.0);
        return disparityFloat;
    }

    public static Mat normalizeForVisualization(Mat src) {
        Mat visualization = new Mat();
        Core.normalize(src, visualization, 0, 255, Core.NORM_MINMAX, CvType.CV_8U);
        return visualization;
    }

    public static Mat createValidValueMask(Mat src, double minValidValue) {
        Mat mask = Mat.zeros(src.size(), CvType.CV_8U);
        for (int row = 0; row < src.rows(); row++) {
            for (int col = 0; col < src.cols(); col++) {
                double value = src.get(row, col)[0];
                if (value > minValidValue && Double.isFinite(value)) {
                    mask.put(row, col, 255);
                }
            }
        }
        return mask;
    }

    public static Mat normalizeForVisualization(Mat src, Mat validMask) {
        Mat visualization = Mat.zeros(src.size(), CvType.CV_8U);
        Core.MinMaxLocResult minMax = Core.minMaxLoc(src, validMask);
        if (minMax.maxVal <= minMax.minVal) {
            return visualization;
        }

        double scale = 255.0 / (minMax.maxVal - minMax.minVal);
        double shift = -minMax.minVal * scale;
        src.convertTo(visualization, CvType.CV_8U, scale, shift);
        visualization.setTo(new Scalar(0), invertedMask(validMask));
        return visualization;
    }

    public static Mat applyHeatMap(Mat normalizedGray) {
        Mat heatMap = new Mat();
        Imgproc.applyColorMap(normalizedGray, heatMap, Imgproc.COLORMAP_TURBO);
        return heatMap;
    }

    public static Mat createHeatMap(Mat src, Mat validMask) {
        Mat normalized = normalizeForVisualization(src, validMask);
        Mat heatMap = applyHeatMap(normalized);
        heatMap.setTo(new Scalar(0, 0, 0), invertedMask(validMask));
        return heatMap;
    }

    public static Mat createInvalidMaskVisualization(Mat validMask) {
        Mat invalidMask = invertedMask(validMask);
        Mat visualization = Mat.zeros(validMask.size(), CvType.CV_8UC3);
        visualization.setTo(new Scalar(0, 0, 255), invalidMask);
        return visualization;
    }

    private static Mat invertedMask(Mat mask) {
        Mat inverted = new Mat();
        Core.bitwise_not(mask, inverted);
        return inverted;
    }
}
