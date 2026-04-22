package stereovision.algorithm;

import org.opencv.calib3d.StereoBM;
import org.opencv.core.Mat;

public class StereoBMAlgorithm extends StereoMatcherAlgorithm {
    public StereoBMAlgorithm() {
        super("StereoBM", new AlgorithmConfig(0, 64, 15));
    }

    public StereoBMAlgorithm(AlgorithmConfig config) {
        super("StereoBM", config);
    }

    @Override
    public Mat computeDisparity(Mat leftGray, Mat rightGray) {
        StereoBM stereoBM = StereoBM.create(config.getNumDisparities(), config.getBlockSize());
        stereoBM.setMinDisparity(config.getMinDisparity());

        Mat disparity16S = new Mat();
        stereoBM.compute(leftGray, rightGray, disparity16S);
        return disparity16S;
    }
}
