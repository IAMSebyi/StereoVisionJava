package stereovision.algorithm;

import org.opencv.core.Mat;

public abstract class StereoMatcherAlgorithm {
    protected String algorithmName;
    protected AlgorithmConfig config;

    protected StereoMatcherAlgorithm(String algorithmName, AlgorithmConfig config) {
        this.algorithmName = algorithmName;
        this.config = config;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public AlgorithmConfig getConfig() {
        return config;
    }

    public void setConfig(AlgorithmConfig config) {
        this.config = config;
    }

    public abstract Mat computeDisparity(Mat leftGray, Mat rightGray);

    @Override
    public String toString() {
        return algorithmName + " " + config;
    }
}
