package stereovision.model;

public class CameraParameters extends BaseEntity {
    private double fx;
    private double fy;
    private double cx;
    private double cy;
    private double baseline;
    private boolean approximate;

    public CameraParameters() {
    }

    public CameraParameters(int id, double fx, double fy, double cx, double cy, double baseline, boolean approximate) {
        super(id);
        this.fx = fx;
        this.fy = fy;
        this.cx = cx;
        this.cy = cy;
        this.baseline = baseline;
        this.approximate = approximate;
    }

    public static CameraParameters createRelativeDefault(int imageWidth, int imageHeight) {
        double fx = Math.max(imageWidth, imageHeight);
        double fy = fx;
        double cx = imageWidth / 2.0;
        double cy = imageHeight / 2.0;
        double baseline = 1.0;
        return new CameraParameters(0, fx, fy, cx, cy, baseline, true);
    }

    public double getFx() {
        return fx;
    }

    public void setFx(double fx) {
        this.fx = fx;
    }

    public double getFy() {
        return fy;
    }

    public void setFy(double fy) {
        this.fy = fy;
    }

    public double getCx() {
        return cx;
    }

    public void setCx(double cx) {
        this.cx = cx;
    }

    public double getCy() {
        return cy;
    }

    public void setCy(double cy) {
        this.cy = cy;
    }

    public double getBaseline() {
        return baseline;
    }

    public void setBaseline(double baseline) {
        this.baseline = baseline;
    }

    public boolean isApproximate() {
        return approximate;
    }

    public void setApproximate(boolean approximate) {
        this.approximate = approximate;
    }

    @Override
    public String toString() {
        return "CameraParameters{" +
                "fx=" + fx +
                ", fy=" + fy +
                ", cx=" + cx +
                ", cy=" + cy +
                ", baseline=" + baseline +
                ", approximate=" + approximate +
                '}';
    }
}
