package stereovision.model;

public class DepthMapResult extends BaseEntity {
    private int pairId;
    private String outputPath;
    private boolean relativeDepth;
    private double minDepth;
    private double maxDepth;

    public DepthMapResult() {
    }

    public DepthMapResult(int id, int pairId, String outputPath, boolean relativeDepth, double minDepth, double maxDepth) {
        super(id);
        this.pairId = pairId;
        this.outputPath = outputPath;
        this.relativeDepth = relativeDepth;
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
    }

    public int getPairId() {
        return pairId;
    }

    public void setPairId(int pairId) {
        this.pairId = pairId;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public boolean isRelativeDepth() {
        return relativeDepth;
    }

    public void setRelativeDepth(boolean relativeDepth) {
        this.relativeDepth = relativeDepth;
    }

    public double getMinDepth() {
        return minDepth;
    }

    public void setMinDepth(double minDepth) {
        this.minDepth = minDepth;
    }

    public double getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(double maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    public String toString() {
        return "DepthMapResult{" +
                "pairId=" + pairId +
                ", outputPath='" + outputPath + '\'' +
                ", relativeDepth=" + relativeDepth +
                ", minDepth=" + minDepth +
                ", maxDepth=" + maxDepth +
                '}';
    }
}
