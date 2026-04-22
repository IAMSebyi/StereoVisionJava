package stereovision.model;

public class DisparityMapResult extends BaseEntity {
    private int pairId;
    private String algorithmName;
    private String outputPath;
    private double minValue;
    private double maxValue;

    public DisparityMapResult() {
    }

    public DisparityMapResult(int id, int pairId, String algorithmName, String outputPath, double minValue, double maxValue) {
        super(id);
        this.pairId = pairId;
        this.algorithmName = algorithmName;
        this.outputPath = outputPath;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public int getPairId() {
        return pairId;
    }

    public void setPairId(int pairId) {
        this.pairId = pairId;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public String toString() {
        return "DisparityMapResult{" +
                "pairId=" + pairId +
                ", algorithmName='" + algorithmName + '\'' +
                ", outputPath='" + outputPath + '\'' +
                ", minValue=" + minValue +
                ", maxValue=" + maxValue +
                '}';
    }
}
