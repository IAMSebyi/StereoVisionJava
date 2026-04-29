package stereovision.repository;

import stereovision.database.DatabaseReadService;
import stereovision.database.DatabaseWriteService;
import stereovision.model.CameraParameters;

import java.util.List;
import java.util.Optional;

public class CameraParametersRepository implements CrudRepository<CameraParameters> {
    private final DatabaseReadService readService;
    private final DatabaseWriteService writeService;

    public CameraParametersRepository() {
        this.readService = DatabaseReadService.getInstance();
        this.writeService = DatabaseWriteService.getInstance();
    }

    public CameraParameters upsert(int projectId, CameraParameters entity) {
        Optional<CameraParameters> existing = readByProjectId(projectId);
        if (existing.isPresent()) {
            entity.setId(existing.get().getId());
            updateForProject(projectId, entity);
            return entity;
        }
        int generatedId = writeService.executeInsert(
                "INSERT INTO camera_parameters(project_id, fx, fy, cx, cy, baseline, approximate) VALUES(?, ?, ?, ?, ?, ?, ?)",
                statement -> {
                    statement.setInt(1, projectId);
                    statement.setDouble(2, entity.getFx());
                    statement.setDouble(3, entity.getFy());
                    statement.setDouble(4, entity.getCx());
                    statement.setDouble(5, entity.getCy());
                    statement.setDouble(6, entity.getBaseline());
                    statement.setInt(7, entity.isApproximate() ? 1 : 0);
                }
        );
        entity.setId(generatedId);
        return entity;
    }

    private void updateForProject(int projectId, CameraParameters entity) {
        writeService.executeUpdate(
                "UPDATE camera_parameters SET fx = ?, fy = ?, cx = ?, cy = ?, baseline = ?, approximate = ? WHERE project_id = ?",
                statement -> {
                    statement.setDouble(1, entity.getFx());
                    statement.setDouble(2, entity.getFy());
                    statement.setDouble(3, entity.getCx());
                    statement.setDouble(4, entity.getCy());
                    statement.setDouble(5, entity.getBaseline());
                    statement.setInt(6, entity.isApproximate() ? 1 : 0);
                    statement.setInt(7, projectId);
                }
        );
    }

    public Optional<CameraParameters> readByProjectId(int projectId) {
        return readService.queryOne(
                "SELECT id, fx, fy, cx, cy, baseline, approximate FROM camera_parameters WHERE project_id = ?",
                statement -> statement.setInt(1, projectId),
                resultSet -> new CameraParameters(
                        resultSet.getInt("id"),
                        resultSet.getDouble("fx"),
                        resultSet.getDouble("fy"),
                        resultSet.getDouble("cx"),
                        resultSet.getDouble("cy"),
                        resultSet.getDouble("baseline"),
                        resultSet.getInt("approximate") == 1
                )
        );
    }

    public void deleteByProjectId(int projectId) {
        writeService.executeUpdate(
                "DELETE FROM camera_parameters WHERE project_id = ?",
                statement -> statement.setInt(1, projectId)
        );
    }

    @Override
    public CameraParameters create(CameraParameters entity) {
        throw new UnsupportedOperationException("Use upsert(projectId, entity) for camera parameters.");
    }

    @Override
    public Optional<CameraParameters> read(int id) {
        return readService.queryOne(
                "SELECT id, fx, fy, cx, cy, baseline, approximate FROM camera_parameters WHERE id = ?",
                statement -> statement.setInt(1, id),
                resultSet -> new CameraParameters(
                        resultSet.getInt("id"),
                        resultSet.getDouble("fx"),
                        resultSet.getDouble("fy"),
                        resultSet.getDouble("cx"),
                        resultSet.getDouble("cy"),
                        resultSet.getDouble("baseline"),
                        resultSet.getInt("approximate") == 1
                )
        );
    }

    @Override
    public List<CameraParameters> readAll() {
        return readService.queryMany(
                "SELECT id, fx, fy, cx, cy, baseline, approximate FROM camera_parameters ORDER BY id",
                null,
                resultSet -> new CameraParameters(
                        resultSet.getInt("id"),
                        resultSet.getDouble("fx"),
                        resultSet.getDouble("fy"),
                        resultSet.getDouble("cx"),
                        resultSet.getDouble("cy"),
                        resultSet.getDouble("baseline"),
                        resultSet.getInt("approximate") == 1
                )
        );
    }

    @Override
    public void update(CameraParameters entity) {
        writeService.executeUpdate(
                "UPDATE camera_parameters SET fx = ?, fy = ?, cx = ?, cy = ?, baseline = ?, approximate = ? WHERE id = ?",
                statement -> {
                    statement.setDouble(1, entity.getFx());
                    statement.setDouble(2, entity.getFy());
                    statement.setDouble(3, entity.getCx());
                    statement.setDouble(4, entity.getCy());
                    statement.setDouble(5, entity.getBaseline());
                    statement.setInt(6, entity.isApproximate() ? 1 : 0);
                    statement.setInt(7, entity.getId());
                }
        );
    }

    @Override
    public void delete(int id) {
        writeService.executeUpdate(
                "DELETE FROM camera_parameters WHERE id = ?",
                statement -> statement.setInt(1, id)
        );
    }
}
