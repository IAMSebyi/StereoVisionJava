package stereovision.model;

public class StereoImagePair extends BaseEntity {
    private String label;
    private String leftImagePath;
    private String rightImagePath;
    private int imageWidth;
    private int imageHeight;

    public StereoImagePair() {
    }

    public StereoImagePair(int id, String label, String leftImagePath, String rightImagePath, int imageWidth, int imageHeight) {
        super(id);
        this.label = label;
        this.leftImagePath = leftImagePath;
        this.rightImagePath = rightImagePath;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLeftImagePath() {
        return leftImagePath;
    }

    public void setLeftImagePath(String leftImagePath) {
        this.leftImagePath = leftImagePath;
    }

    public String getRightImagePath() {
        return rightImagePath;
    }

    public void setRightImagePath(String rightImagePath) {
        this.rightImagePath = rightImagePath;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    @Override
    public String toString() {
        return "StereoImagePair{" +
                "id=" + id +
                ", label='" + label + '\'' +
                ", leftImagePath='" + leftImagePath + '\'' +
                ", rightImagePath='" + rightImagePath + '\'' +
                ", imageWidth=" + imageWidth +
                ", imageHeight=" + imageHeight +
                '}';
    }
}
