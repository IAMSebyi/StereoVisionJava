package stereovision.model;

public class ReconstructionStats {
    private int totalPixels;
    private int validDepthPixels;
    private int pointCount;
    private double minDepth;
    private double maxDepth;
    private double meanDepth;

    public ReconstructionStats() {
    }

    public ReconstructionStats(int totalPixels, int validDepthPixels, int pointCount, double minDepth, double maxDepth, double meanDepth) {
        this.totalPixels = totalPixels;
        this.validDepthPixels = validDepthPixels;
        this.pointCount = pointCount;
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
        this.meanDepth = meanDepth;
    }

    public int getTotalPixels() {
        return totalPixels;
    }

    public void setTotalPixels(int totalPixels) {
        this.totalPixels = totalPixels;
    }

    public int getValidDepthPixels() {
        return validDepthPixels;
    }

    public void setValidDepthPixels(int validDepthPixels) {
        this.validDepthPixels = validDepthPixels;
    }

    public int getPointCount() {
        return pointCount;
    }

    public void setPointCount(int pointCount) {
        this.pointCount = pointCount;
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

    public double getMeanDepth() {
        return meanDepth;
    }

    public void setMeanDepth(double meanDepth) {
        this.meanDepth = meanDepth;
    }

    @Override
    public String toString() {
        return "ReconstructionStats{" +
                "totalPixels=" + totalPixels +
                ", validDepthPixels=" + validDepthPixels +
                ", pointCount=" + pointCount +
                ", minDepth=" + minDepth +
                ", maxDepth=" + maxDepth +
                ", meanDepth=" + meanDepth +
                '}';
    }
}
