package stereovision.repository;

import stereovision.database.DatabaseReadService;
import stereovision.database.DatabaseWriteService;
import stereovision.model.ReconstructionSession;
import stereovision.model.ReconstructionStats;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ReconstructionSessionRepository implements CrudRepository<ReconstructionSession> {
    private final DatabaseReadService readService;
    private final DatabaseWriteService writeService;

    public ReconstructionSessionRepository() {
        this.readService = DatabaseReadService.getInstance();
        this.writeService = DatabaseWriteService.getInstance();
    }

    @Override
    public ReconstructionSession create(ReconstructionSession entity) {
        int generatedId = writeService.executeInsert(
                """
                INSERT INTO reconstruction_sessions(
                    project_id, pair_id, algorithm_name,
                    disparity_output_path, disparity_heatmap_output_path, invalid_disparity_mask_output_path,
                    depth_output_path, depth_heatmap_output_path, points_output_path,
                    total_pixels, valid_depth_pixels, point_count, min_depth, max_depth, mean_depth, created_at
                ) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                statement -> {
                    statement.setInt(1, entity.getProjectId());
                    statement.setInt(2, entity.getPairId());
                    statement.setString(3, entity.getAlgorithmName());
                    statement.setString(4, entity.getDisparityOutputPath());
                    statement.setString(5, entity.getDisparityHeatMapOutputPath());
                    statement.setString(6, entity.getInvalidDisparityMaskOutputPath());
                    statement.setString(7, entity.getDepthOutputPath());
                    statement.setString(8, entity.getDepthHeatMapOutputPath());
                    statement.setString(9, entity.getPointsOutputPath());
                    ReconstructionStats stats = entity.getStats();
                    statement.setInt(10, stats != null ? stats.getTotalPixels() : 0);
                    statement.setInt(11, stats != null ? stats.getValidDepthPixels() : 0);
                    statement.setInt(12, stats != null ? stats.getPointCount() : 0);
                    statement.setDouble(13, stats != null ? stats.getMinDepth() : 0.0);
                    statement.setDouble(14, stats != null ? stats.getMaxDepth() : 0.0);
                    statement.setDouble(15, stats != null ? stats.getMeanDepth() : 0.0);
                    LocalDateTime createdAt = entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now();
                    entity.setCreatedAt(createdAt);
                    statement.setString(16, createdAt.toString());
                }
        );
        entity.setId(generatedId);
        return entity;
    }

    @Override
    public Optional<ReconstructionSession> read(int id) {
        return readService.queryOne(
                "SELECT * FROM reconstruction_sessions WHERE id = ?",
                statement -> statement.setInt(1, id),
                this::mapSession
        );
    }

    public List<ReconstructionSession> readByProjectId(int projectId) {
        return readService.queryMany(
                "SELECT * FROM reconstruction_sessions WHERE project_id = ? ORDER BY id",
                statement -> statement.setInt(1, projectId),
                this::mapSession
        );
    }

    @Override
    public List<ReconstructionSession> readAll() {
        return readService.queryMany(
                "SELECT * FROM reconstruction_sessions ORDER BY id",
                null,
                this::mapSession
        );
    }

    @Override
    public void update(ReconstructionSession entity) {
        writeService.executeUpdate(
                """
                UPDATE reconstruction_sessions SET
                    project_id = ?, pair_id = ?, algorithm_name = ?,
                    disparity_output_path = ?, disparity_heatmap_output_path = ?, invalid_disparity_mask_output_path = ?,
                    depth_output_path = ?, depth_heatmap_output_path = ?, points_output_path = ?,
                    total_pixels = ?, valid_depth_pixels = ?, point_count = ?, min_depth = ?, max_depth = ?, mean_depth = ?, created_at = ?
                WHERE id = ?
                """,
                statement -> {
                    statement.setInt(1, entity.getProjectId());
                    statement.setInt(2, entity.getPairId());
                    statement.setString(3, entity.getAlgorithmName());
                    statement.setString(4, entity.getDisparityOutputPath());
                    statement.setString(5, entity.getDisparityHeatMapOutputPath());
                    statement.setString(6, entity.getInvalidDisparityMaskOutputPath());
                    statement.setString(7, entity.getDepthOutputPath());
                    statement.setString(8, entity.getDepthHeatMapOutputPath());
                    statement.setString(9, entity.getPointsOutputPath());
                    ReconstructionStats stats = entity.getStats();
                    statement.setInt(10, stats != null ? stats.getTotalPixels() : 0);
                    statement.setInt(11, stats != null ? stats.getValidDepthPixels() : 0);
                    statement.setInt(12, stats != null ? stats.getPointCount() : 0);
                    statement.setDouble(13, stats != null ? stats.getMinDepth() : 0.0);
                    statement.setDouble(14, stats != null ? stats.getMaxDepth() : 0.0);
                    statement.setDouble(15, stats != null ? stats.getMeanDepth() : 0.0);
                    statement.setString(16, entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : LocalDateTime.now().toString());
                    statement.setInt(17, entity.getId());
                }
        );
    }

    @Override
    public void delete(int id) {
        writeService.executeUpdate(
                "DELETE FROM reconstruction_sessions WHERE id = ?",
                statement -> statement.setInt(1, id)
        );
    }

    private ReconstructionSession mapSession(java.sql.ResultSet resultSet) throws java.sql.SQLException {
        ReconstructionSession session = new ReconstructionSession(
                resultSet.getInt("id"),
                resultSet.getInt("project_id"),
                resultSet.getInt("pair_id"),
                resultSet.getString("algorithm_name")
        );
        session.setDisparityOutputPath(resultSet.getString("disparity_output_path"));
        session.setDisparityHeatMapOutputPath(resultSet.getString("disparity_heatmap_output_path"));
        session.setInvalidDisparityMaskOutputPath(resultSet.getString("invalid_disparity_mask_output_path"));
        session.setDepthOutputPath(resultSet.getString("depth_output_path"));
        session.setDepthHeatMapOutputPath(resultSet.getString("depth_heatmap_output_path"));
        session.setPointsOutputPath(resultSet.getString("points_output_path"));
        session.setStats(new ReconstructionStats(
                resultSet.getInt("total_pixels"),
                resultSet.getInt("valid_depth_pixels"),
                resultSet.getInt("point_count"),
                resultSet.getDouble("min_depth"),
                resultSet.getDouble("max_depth"),
                resultSet.getDouble("mean_depth")
        ));
        session.setCreatedAt(LocalDateTime.parse(resultSet.getString("created_at")));
        return session;
    }
}
