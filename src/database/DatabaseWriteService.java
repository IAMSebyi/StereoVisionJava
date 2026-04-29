package stereovision.database;

import stereovision.config.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseWriteService {
    private static DatabaseWriteService instance;
    private final DatabaseConnectionManager connectionManager;

    private DatabaseWriteService() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }

    public static synchronized DatabaseWriteService getInstance() {
        if (instance == null) {
            instance = new DatabaseWriteService();
        }
        return instance;
    }

    public int executeUpdate(String sql, SqlConsumer<PreparedStatement> binder) {
        try {
            Connection connection = connectionManager.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (binder != null) {
                    binder.accept(statement);
                }
                return statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Database write failed.", exception);
        }
    }

    public int executeInsert(String sql, SqlConsumer<PreparedStatement> binder) {
        try {
            Connection connection = connectionManager.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                if (binder != null) {
                    binder.accept(statement);
                }
                statement.executeUpdate();
                try (var generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
                throw new IllegalStateException("Insert succeeded but no generated key was returned.");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Database insert failed.", exception);
        }
    }
}
