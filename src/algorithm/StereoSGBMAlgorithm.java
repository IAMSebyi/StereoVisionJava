package stereovision.algorithm;

import org.opencv.calib3d.StereoSGBM;
import org.opencv.core.Mat;

public class StereoSGBMAlgorithm extends StereoMatcherAlgorithm {
    public StereoSGBMAlgorithm() {
        super("StereoSGBM", new AlgorithmConfig(0, 64, 5));
    }

    public StereoSGBMAlgorithm(AlgorithmConfig config) {
        super("StereoSGBM", config);
    }

    @Override
    public Mat computeDisparity(Mat leftGray, Mat rightGray) {
        StereoSGBM stereoSGBM = StereoSGBM.create(
                config.getMinDisparity(),
                config.getNumDisparities(),
                config.getBlockSize()
        );

        Mat disparity16S = new Mat();
        stereoSGBM.compute(leftGray, rightGray, disparity16S);
        return disparity16S;
    }
}
