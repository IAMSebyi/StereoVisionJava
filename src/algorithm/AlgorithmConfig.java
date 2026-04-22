package stereovision.algorithm;

public class AlgorithmConfig {
    private int minDisparity;
    private int numDisparities;
    private int blockSize;

    public AlgorithmConfig() {
        this(0, 64, 15);
    }

    public AlgorithmConfig(int minDisparity, int numDisparities, int blockSize) {
        this.minDisparity = minDisparity;
        this.numDisparities = ensureDivisibleBy16(numDisparities);
        this.blockSize = ensureOdd(blockSize);
    }

    private int ensureDivisibleBy16(int value) {
        if (value <= 0) {
            return 64;
        }
        return ((value + 15) / 16) * 16;
    }

    private int ensureOdd(int value) {
        int adjusted = Math.max(5, value);
        return adjusted % 2 == 0 ? adjusted + 1 : adjusted;
    }

    public int getMinDisparity() {
        return minDisparity;
    }

    public void setMinDisparity(int minDisparity) {
        this.minDisparity = minDisparity;
    }

    public int getNumDisparities() {
        return numDisparities;
    }

    public void setNumDisparities(int numDisparities) {
        this.numDisparities = ensureDivisibleBy16(numDisparities);
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = ensureOdd(blockSize);
    }

    @Override
    public String toString() {
        return "AlgorithmConfig{" +
                "minDisparity=" + minDisparity +
                ", numDisparities=" + numDisparities +
                ", blockSize=" + blockSize +
                '}';
    }
}
