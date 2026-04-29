package stereovision.database;

import stereovision.config.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class DatabaseReadService {
    private static DatabaseReadService instance;
    private final DatabaseConnectionManager connectionManager;

    private DatabaseReadService() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }

    public static synchronized DatabaseReadService getInstance() {
        if (instance == null) {
            instance = new DatabaseReadService();
        }
        return instance;
    }

    public <T> List<T> queryMany(String sql,
                                 SqlConsumer<PreparedStatement> binder,
                                 SqlFunction<ResultSet, T> mapper) {
        List<T> results = new ArrayList<>();
        try {
            Connection connection = connectionManager.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (binder != null) {
                    binder.accept(statement);
                }
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        results.add(mapper.apply(resultSet));
                    }
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Database read failed.", exception);
        }
        return results;
    }

    public <T> Optional<T> queryOne(String sql,
                                    SqlConsumer<PreparedStatement> binder,
                                    SqlFunction<ResultSet, T> mapper) {
        List<T> results = queryMany(sql, binder, mapper);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
