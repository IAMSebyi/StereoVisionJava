package stereovision.service;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import stereovision.model.Point3DData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class ExportService {
    public String exportImage(Mat image, String outputPath) {
        ensureParentDirectory(outputPath);
        boolean written = Imgcodecs.imwrite(outputPath, image);
        if (!written) {
            throw new IllegalStateException("Could not save image file: " + outputPath);
        }
        return outputPath;
    }

    public String exportPointsCsv(List<Point3DData> points, String outputPath) {
        ensureParentDirectory(outputPath);
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputPath)))) {
            writer.println("x,y,z,row,col");
            for (Point3DData point : points) {
                writer.printf("%f,%f,%f,%d,%d%n",
                        point.getX(), point.getY(), point.getZ(), point.getRow(), point.getCol());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not save CSV file: " + outputPath, e);
        }
        return outputPath;
    }

    private void ensureParentDirectory(String outputPath) {
        File file = new File(outputPath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            boolean created = parent.mkdirs();
            if (!created && !parent.exists()) {
                throw new IllegalStateException("Could not create directory: " + parent.getAbsolutePath());
            }
        }
    }
}
