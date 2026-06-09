package stereovision.repository;

import stereovision.database.DatabaseReadService;
import stereovision.database.DatabaseWriteService;
import stereovision.model.UserAccount;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class UserRepository implements CrudRepository<UserAccount> {
    private final DatabaseReadService readService;
    private final DatabaseWriteService writeService;

    public UserRepository() {
        this.readService = DatabaseReadService.getInstance();
        this.writeService = DatabaseWriteService.getInstance();
    }

    @Override
    public UserAccount create(UserAccount entity) {
        int generatedId = writeService.executeInsert(
                "INSERT INTO users(username, password_hash, created_at) VALUES(?, ?, ?)",
                statement -> {
                    statement.setString(1, entity.getUsername());
                    statement.setString(2, entity.getPasswordHash());
                    statement.setString(3, entity.getCreatedAt().toString());
                }
        );
        entity.setId(generatedId);
        return entity;
    }

    @Override
    public Optional<UserAccount> read(int id) {
        return readService.queryOne(
                "SELECT id, username, password_hash, created_at FROM users WHERE id = ?",
                statement -> statement.setInt(1, id),
                resultSet -> new UserAccount(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password_hash"),
                        LocalDateTime.parse(resultSet.getString("created_at"))
                )
        );
    }

    public Optional<UserAccount> readByUsername(String username) {
        return readService.queryOne(
                "SELECT id, username, password_hash, created_at FROM users WHERE lower(username) = lower(?)",
                statement -> statement.setString(1, username),
                resultSet -> new UserAccount(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password_hash"),
                        LocalDateTime.parse(resultSet.getString("created_at"))
                )
        );
    }

    @Override
    public List<UserAccount> readAll() {
        return readService.queryMany(
                "SELECT id, username, password_hash, created_at FROM users ORDER BY id",
                null,
                resultSet -> new UserAccount(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password_hash"),
                        LocalDateTime.parse(resultSet.getString("created_at"))
                )
        );
    }

    @Override
    public void update(UserAccount entity) {
        writeService.executeUpdate(
                "UPDATE users SET username = ?, password_hash = ?, created_at = ? WHERE id = ?",
                statement -> {
                    statement.setString(1, entity.getUsername());
                    statement.setString(2, entity.getPasswordHash());
                    statement.setString(3, entity.getCreatedAt().toString());
                    statement.setInt(4, entity.getId());
                }
        );
    }

    @Override
    public void delete(int id) {
        writeService.executeUpdate(
                "DELETE FROM users WHERE id = ?",
                statement -> statement.setInt(1, id)
        );
    }
}
