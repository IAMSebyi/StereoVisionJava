package stereovision.service;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import stereovision.algorithm.StereoBMAlgorithm;
import stereovision.algorithm.StereoMatcherAlgorithm;
import stereovision.algorithm.StereoSGBMAlgorithm;
import stereovision.config.DatabaseInitializer;
import stereovision.model.CameraParameters;
import stereovision.model.DepthMapResult;
import stereovision.model.DisparityMapResult;
import stereovision.model.Point3DData;
import stereovision.model.ReconstructionSession;
import stereovision.model.ReconstructionStats;
import stereovision.model.StereoImagePair;
import stereovision.model.StereoProject;
import stereovision.repository.CameraParametersRepository;
import stereovision.repository.ReconstructionSessionRepository;
import stereovision.util.ExifCameraEstimator;
import stereovision.util.ImageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class ReconstructionService {
    private final List<StereoMatcherAlgorithm> availableAlgorithms;
    private final Map<String, StereoMatcherAlgorithm> algorithmsByName;
    private final ExportService exportService;
    private final ReconstructionSessionRepository sessionRepository;
    private final CameraParametersRepository cameraParametersRepository;
    private final AuditService auditService;

    public ReconstructionService() {
        DatabaseInitializer.initialize();
        this.availableAlgorithms = new ArrayList<>();
        this.algorithmsByName = new HashMap<>();
        this.exportService = new ExportService();
        this.sessionRepository = new ReconstructionSessionRepository();
        this.cameraParametersRepository = new CameraParametersRepository();
        this.auditService = AuditService.getInstance();

        registerAlgorithm(new StereoBMAlgorithm());
        registerAlgorithm(new StereoSGBMAlgorithm());
    }

    private void registerAlgorithm(StereoMatcherAlgorithm algorithm) {
        availableAlgorithms.add(algorithm);
        algorithmsByName.put(algorithm.getAlgorithmName().toLowerCase(), algorithm);
    }

    public List<StereoMatcherAlgorithm> getAvailableAlgorithms() {
        return new ArrayList<>(availableAlgorithms);
    }

    public ReconstructionSession runReconstruction(StereoProject project,
                                                   StereoImagePair pair,
                                                   String algorithmName,
                                                   String outputDirectory) {
        StereoMatcherAlgorithm algorithm = algorithmsByName.get(algorithmName.toLowerCase());
        if (algorithm == null) {
            throw new IllegalArgumentException("Unknown algorithm: " + algorithmName);
        }

        Mat leftColor = ImageUtils.loadColor(pair.getLeftImagePath());
        Mat rightColor = ImageUtils.loadColor(pair.getRightImagePath());
        ImageUtils.validateLoaded(leftColor, pair.getLeftImagePath());
        ImageUtils.validateLoaded(rightColor, pair.getRightImagePath());
        ImageUtils.validateSameSize(leftColor, rightColor);

        Mat leftGray = ImageUtils.toGray(leftColor);
        Mat rightGray = ImageUtils.toGray(rightColor);
        Mat disparity16S = algorithm.computeDisparity(leftGray, rightGray);
        Mat disparityFloat = ImageUtils.disparity16SToFloat(disparity16S);
        Mat validDisparityMask = ImageUtils.createValidValueMask(disparityFloat, 0.1);
        Mat disparityVisualization = ImageUtils.normalizeForVisualization(disparityFloat, validDisparityMask);
        Mat disparityHeatMap = ImageUtils.createHeatMap(disparityFloat, validDisparityMask);
        Mat invalidDisparityMaskVisualization = ImageUtils.createInvalidMaskVisualization(validDisparityMask);

        CameraParameters cameraParameters = project.getCameraParameters();
        if (cameraParameters == null) {
            cameraParameters = ExifCameraEstimator.estimateOrDefault(
                    pair.getLeftImagePath(),
                    pair.getRightImagePath(),
                    pair.getImageWidth(),
                    pair.getImageHeight()
            );
            cameraParametersRepository.upsert(project.getId(), cameraParameters);
            project.setCameraParameters(cameraParameters);
        }

        Mat depthFloat = computeRelativeDepth(disparityFloat);
        Mat validDepthMask = ImageUtils.createValidValueMask(depthFloat, 0.0);
        Mat depthVisualization = ImageUtils.normalizeForVisualization(depthFloat, validDepthMask);
        Mat depthHeatMap = ImageUtils.createHeatMap(depthFloat, validDepthMask);
        Mat qMatrix = buildQMatrix(cameraParameters);
        Set<Point3DData> sortedPoints = generateRelativePointCloud(disparityFloat, depthFloat, qMatrix, 12);
        ReconstructionStats stats = computeStats(depthFloat, sortedPoints.size());

        String baseName = sanitizeFileName(project.getName()) + "_pair_" + pair.getId() + "_" + algorithm.getAlgorithmName().toLowerCase();
        String disparityPath = new File(outputDirectory, baseName + "_disparity.png").getPath();
        String disparityHeatMapPath = new File(outputDirectory, baseName + "_disparity_heatmap.png").getPath();
        String invalidDisparityMaskPath = new File(outputDirectory, baseName + "_invalid_disparity_mask.png").getPath();
        String depthPath = new File(outputDirectory, baseName + "_depth.png").getPath();
        String depthHeatMapPath = new File(outputDirectory, baseName + "_depth_heatmap.png").getPath();
        String pointsPath = new File(outputDirectory, baseName + "_points.csv").getPath();

        exportService.exportImage(disparityVisualization, disparityPath);
        exportService.exportImage(disparityHeatMap, disparityHeatMapPath);
        exportService.exportImage(invalidDisparityMaskVisualization, invalidDisparityMaskPath);
        exportService.exportImage(depthVisualization, depthPath);
        exportService.exportImage(depthHeatMap, depthHeatMapPath);
        exportService.exportPointsCsv(new ArrayList<>(sortedPoints), pointsPath);

        Core.MinMaxLocResult disparityMinMax = Core.minMaxLoc(disparityFloat);
        Core.MinMaxLocResult depthMinMax = Core.minMaxLoc(depthFloat);

        DisparityMapResult disparityResult = new DisparityMapResult(
                0,
                pair.getId(),
                algorithm.getAlgorithmName(),
                disparityPath,
                disparityMinMax.minVal,
                disparityMinMax.maxVal
        );

        DepthMapResult depthResult = new DepthMapResult(
                0,
                pair.getId(),
                depthPath,
                true,
                depthMinMax.minVal,
                depthMinMax.maxVal
        );

        ReconstructionSession session = new ReconstructionSession(0, project.getId(), pair.getId(), algorithm.getAlgorithmName());
        session.setDisparityResult(disparityResult);
        session.setDepthResult(depthResult);
        session.setDisparityOutputPath(disparityPath);
        session.setDisparityHeatMapOutputPath(disparityHeatMapPath);
        session.setInvalidDisparityMaskOutputPath(invalidDisparityMaskPath);
        session.setDepthOutputPath(depthPath);
        session.setDepthHeatMapOutputPath(depthHeatMapPath);
        session.setPointsOutputPath(pointsPath);
        session.setPoints(new ArrayList<>(sortedPoints));
        session.setStats(stats);

        sessionRepository.create(session);
        project.addSession(session);
        auditService.logAction("run_reconstruction_" + algorithm.getAlgorithmName().toLowerCase());
        return session;
    }

    public List<ReconstructionSession> getSessionsForProject(int projectId) {
        return sessionRepository.readByProjectId(projectId);
    }

    public Optional<ReconstructionSession> getSessionById(int sessionId) {
        return sessionRepository.read(sessionId);
    }

    public void updateSession(ReconstructionSession session) {
        sessionRepository.update(session);
    }

    public void deleteSession(StereoProject project, int sessionId) {
        sessionRepository.delete(sessionId);
        project.getSessions().removeIf(session -> session.getId() == sessionId);
        auditService.logAction("delete_reconstruction_session");
    }

    private Mat computeRelativeDepth(Mat disparityFloat) {
        Mat depthFloat = Mat.zeros(disparityFloat.size(), CvType.CV_32F);

        for (int row = 0; row < disparityFloat.rows(); row++) {
            for (int col = 0; col < disparityFloat.cols(); col++) {
                double disparity = disparityFloat.get(row, col)[0];
                if (disparity > 0.1) {
                    depthFloat.put(row, col, 1.0 / disparity);
                }
            }
        }

        return depthFloat;
    }

    private Mat buildQMatrix(CameraParameters cameraParameters) {
        Mat q = Mat.zeros(4, 4, CvType.CV_64F);
        q.put(0, 0, 1.0);
        q.put(1, 1, 1.0);
        q.put(0, 3, -cameraParameters.getCx());
        q.put(1, 3, -cameraParameters.getCy());
        q.put(2, 3, cameraParameters.getFx());
        q.put(3, 2, 1.0 / Math.max(cameraParameters.getBaseline(), 1e-6));
        return q;
    }

    private Set<Point3DData> generateRelativePointCloud(Mat disparityFloat, Mat depthFloat, Mat qMatrix, int step) {
        Mat points3D = new Mat();
        Calib3d.reprojectImageTo3D(disparityFloat, points3D, qMatrix);

        Set<Point3DData> points = new TreeSet<>();
        for (int row = 0; row < points3D.rows(); row += step) {
            for (int col = 0; col < points3D.cols(); col += step) {
                double disparity = disparityFloat.get(row, col)[0];
                double depth = depthFloat.get(row, col)[0];
                if (disparity <= 0.1 || depth <= 0.0) {
                    continue;
                }

                double[] xyz = points3D.get(row, col);
                if (xyz == null || xyz.length < 3) {
                    continue;
                }
                if (!Double.isFinite(xyz[0]) || !Double.isFinite(xyz[1]) || !Double.isFinite(xyz[2])) {
                    continue;
                }
                if (Math.abs(xyz[2]) > 10000.0) {
                    continue;
                }

                points.add(new Point3DData(xyz[0], xyz[1], xyz[2], row, col));
            }
        }
        return points;
    }

    private ReconstructionStats computeStats(Mat depthFloat, int pointCount) {
        double minDepth = Double.POSITIVE_INFINITY;
        double maxDepth = Double.NEGATIVE_INFINITY;
        double sumDepth = 0.0;
        int validDepthPixels = 0;

        for (int row = 0; row < depthFloat.rows(); row++) {
            for (int col = 0; col < depthFloat.cols(); col++) {
                double depth = depthFloat.get(row, col)[0];
                if (depth > 0.0 && Double.isFinite(depth)) {
                    validDepthPixels++;
                    sumDepth += depth;
                    minDepth = Math.min(minDepth, depth);
                    maxDepth = Math.max(maxDepth, depth);
                }
            }
        }

        if (validDepthPixels == 0) {
            minDepth = 0.0;
            maxDepth = 0.0;
        }

        double meanDepth = validDepthPixels == 0 ? 0.0 : sumDepth / validDepthPixels;
        return new ReconstructionStats(depthFloat.rows() * depthFloat.cols(), validDepthPixels, pointCount, minDepth, maxDepth, meanDepth);
    }

    private String sanitizeFileName(String input) {
        return input.toLowerCase().replaceAll("[^a-z0-9]+", "_");
    }
}
