package stereovision.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReconstructionSession extends BaseEntity {
    private int projectId;
    private int pairId;
    private String algorithmName;
    private String disparityOutputPath;
    private String disparityHeatMapOutputPath;
    private String invalidDisparityMaskOutputPath;
    private String depthOutputPath;
    private String depthHeatMapOutputPath;
    private String pointsOutputPath;
    private DisparityMapResult disparityResult;
    private DepthMapResult depthResult;
    private ReconstructionStats stats;
    private List<Point3DData> points;
    private LocalDateTime createdAt;

    public ReconstructionSession() {
        this.points = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    public ReconstructionSession(int id, int projectId, int pairId, String algorithmName) {
        super(id);
        this.projectId = projectId;
        this.pairId = pairId;
        this.algorithmName = algorithmName;
        this.points = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
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

    public String getDisparityOutputPath() {
        return disparityOutputPath;
    }

    public void setDisparityOutputPath(String disparityOutputPath) {
        this.disparityOutputPath = disparityOutputPath;
    }

    public String getDisparityHeatMapOutputPath() {
        return disparityHeatMapOutputPath;
    }

    public void setDisparityHeatMapOutputPath(String disparityHeatMapOutputPath) {
        this.disparityHeatMapOutputPath = disparityHeatMapOutputPath;
    }

    public String getInvalidDisparityMaskOutputPath() {
        return invalidDisparityMaskOutputPath;
    }

    public void setInvalidDisparityMaskOutputPath(String invalidDisparityMaskOutputPath) {
        this.invalidDisparityMaskOutputPath = invalidDisparityMaskOutputPath;
    }

    public String getDepthOutputPath() {
        return depthOutputPath;
    }

    public void setDepthOutputPath(String depthOutputPath) {
        this.depthOutputPath = depthOutputPath;
    }

    public String getDepthHeatMapOutputPath() {
        return depthHeatMapOutputPath;
    }

    public void setDepthHeatMapOutputPath(String depthHeatMapOutputPath) {
        this.depthHeatMapOutputPath = depthHeatMapOutputPath;
    }

    public String getPointsOutputPath() {
        return pointsOutputPath;
    }

    public void setPointsOutputPath(String pointsOutputPath) {
        this.pointsOutputPath = pointsOutputPath;
    }

    public DisparityMapResult getDisparityResult() {
        return disparityResult;
    }

    public void setDisparityResult(DisparityMapResult disparityResult) {
        this.disparityResult = disparityResult;
    }

    public DepthMapResult getDepthResult() {
        return depthResult;
    }

    public void setDepthResult(DepthMapResult depthResult) {
        this.depthResult = depthResult;
    }

    public ReconstructionStats getStats() {
        return stats;
    }

    public void setStats(ReconstructionStats stats) {
        this.stats = stats;
    }

    public List<Point3DData> getPoints() {
        return points;
    }

    public void setPoints(List<Point3DData> points) {
        this.points = points;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ReconstructionSession{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", pairId=" + pairId +
                ", algorithmName='" + algorithmName + '\'' +
                ", disparityOutputPath='" + disparityOutputPath + '\'' +
                ", disparityHeatMapOutputPath='" + disparityHeatMapOutputPath + '\'' +
                ", invalidDisparityMaskOutputPath='" + invalidDisparityMaskOutputPath + '\'' +
                ", depthOutputPath='" + depthOutputPath + '\'' +
                ", depthHeatMapOutputPath='" + depthHeatMapOutputPath + '\'' +
                ", pointsOutputPath='" + pointsOutputPath + '\'' +
                ", stats=" + stats +
                ", createdAt=" + createdAt +
                '}';
    }
}
