package stereovision.repository;

import stereovision.database.DatabaseReadService;
import stereovision.database.DatabaseWriteService;
import stereovision.model.StereoImagePair;

import java.util.List;
import java.util.Optional;

public class StereoImagePairRepository implements CrudRepository<StereoImagePair> {
    private final DatabaseReadService readService;
    private final DatabaseWriteService writeService;

    public StereoImagePairRepository() {
        this.readService = DatabaseReadService.getInstance();
        this.writeService = DatabaseWriteService.getInstance();
    }

    public StereoImagePair create(int projectId, StereoImagePair entity) {
        int generatedId = writeService.executeInsert(
                "INSERT INTO stereo_image_pairs(project_id, label, left_image_path, right_image_path, image_width, image_height) VALUES(?, ?, ?, ?, ?, ?)",
                statement -> {
                    statement.setInt(1, projectId);
                    statement.setString(2, entity.getLabel());
                    statement.setString(3, entity.getLeftImagePath());
                    statement.setString(4, entity.getRightImagePath());
                    statement.setInt(5, entity.getImageWidth());
                    statement.setInt(6, entity.getImageHeight());
                }
        );
        entity.setId(generatedId);
        return entity;
    }

    @Override
    public StereoImagePair create(StereoImagePair entity) {
        throw new UnsupportedOperationException("Use create(projectId, entity) for stereo pairs.");
    }

    @Override
    public Optional<StereoImagePair> read(int id) {
        return readService.queryOne(
                "SELECT id, label, left_image_path, right_image_path, image_width, image_height FROM stereo_image_pairs WHERE id = ?",
                statement -> statement.setInt(1, id),
                resultSet -> new StereoImagePair(
                        resultSet.getInt("id"),
                        resultSet.getString("label"),
                        resultSet.getString("left_image_path"),
                        resultSet.getString("right_image_path"),
                        resultSet.getInt("image_width"),
                        resultSet.getInt("image_height")
                )
        );
    }

    public List<StereoImagePair> readByProjectId(int projectId) {
        return readService.queryMany(
                "SELECT id, label, left_image_path, right_image_path, image_width, image_height FROM stereo_image_pairs WHERE project_id = ? ORDER BY id",
                statement -> statement.setInt(1, projectId),
                resultSet -> new StereoImagePair(
                        resultSet.getInt("id"),
                        resultSet.getString("label"),
                        resultSet.getString("left_image_path"),
                        resultSet.getString("right_image_path"),
                        resultSet.getInt("image_width"),
                        resultSet.getInt("image_height")
                )
        );
    }

    @Override
    public List<StereoImagePair> readAll() {
        return readService.queryMany(
                "SELECT id, label, left_image_path, right_image_path, image_width, image_height FROM stereo_image_pairs ORDER BY id",
                null,
                resultSet -> new StereoImagePair(
                        resultSet.getInt("id"),
                        resultSet.getString("label"),
                        resultSet.getString("left_image_path"),
                        resultSet.getString("right_image_path"),
                        resultSet.getInt("image_width"),
                        resultSet.getInt("image_height")
                )
        );
    }

    @Override
    public void update(StereoImagePair entity) {
        writeService.executeUpdate(
                "UPDATE stereo_image_pairs SET label = ?, left_image_path = ?, right_image_path = ?, image_width = ?, image_height = ? WHERE id = ?",
                statement -> {
                    statement.setString(1, entity.getLabel());
                    statement.setString(2, entity.getLeftImagePath());
                    statement.setString(3, entity.getRightImagePath());
                    statement.setInt(4, entity.getImageWidth());
                    statement.setInt(5, entity.getImageHeight());
                    statement.setInt(6, entity.getId());
                }
        );
    }

    @Override
    public void delete(int id) {
        writeService.executeUpdate(
                "DELETE FROM stereo_image_pairs WHERE id = ?",
                statement -> statement.setInt(1, id)
        );
    }
}
