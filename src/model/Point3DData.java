package stereovision.model;

public class Point3DData implements Comparable<Point3DData> {
    private double x;
    private double y;
    private double z;
    private int row;
    private int col;

    public Point3DData(double x, double y, double z, int row, int col) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.row = row;
        this.col = col;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    @Override
    public int compareTo(Point3DData other) {
        int byZ = Double.compare(this.z, other.z);
        if (byZ != 0) {
            return byZ;
        }

        int byRow = Integer.compare(this.row, other.row);
        if (byRow != 0) {
            return byRow;
        }

        return Integer.compare(this.col, other.col);
    }

    @Override
    public String toString() {
        return "Point3DData{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", row=" + row +
                ", col=" + col +
                '}';
    }
}
