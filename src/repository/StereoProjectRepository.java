package stereovision.repository;

import stereovision.database.DatabaseReadService;
import stereovision.database.DatabaseWriteService;
import stereovision.model.StereoProject;

import java.util.List;
import java.util.Optional;

public class StereoProjectRepository implements CrudRepository<StereoProject> {
    private final DatabaseReadService readService;
    private final DatabaseWriteService writeService;

    public StereoProjectRepository() {
        this.readService = DatabaseReadService.getInstance();
        this.writeService = DatabaseWriteService.getInstance();
    }

    @Override
    public StereoProject create(StereoProject entity) {
        int generatedId = writeService.executeInsert(
                "INSERT INTO stereo_projects(name, description) VALUES(?, ?)",
                statement -> {
                    statement.setString(1, entity.getName());
                    statement.setString(2, entity.getDescription());
                }
        );
        entity.setId(generatedId);
        return entity;
    }

    @Override
    public Optional<StereoProject> read(int id) {
        return readService.queryOne(
                "SELECT id, name, description FROM stereo_projects WHERE id = ?",
                statement -> statement.setInt(1, id),
                resultSet -> new StereoProject(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("description")
                )
        );
    }

    @Override
    public List<StereoProject> readAll() {
        return readService.queryMany(
                "SELECT id, name, description FROM stereo_projects ORDER BY id",
                null,
                resultSet -> new StereoProject(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("description")
                )
        );
    }

    @Override
    public void update(StereoProject entity) {
        writeService.executeUpdate(
                "UPDATE stereo_projects SET name = ?, description = ? WHERE id = ?",
                statement -> {
                    statement.setString(1, entity.getName());
                    statement.setString(2, entity.getDescription());
                    statement.setInt(3, entity.getId());
                }
        );
    }

    @Override
    public void delete(int id) {
        writeService.executeUpdate(
                "DELETE FROM stereo_projects WHERE id = ?",
                statement -> statement.setInt(1, id)
        );
    }
}
