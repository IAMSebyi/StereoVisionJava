package stereovision.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConnectionManager {
    private static DatabaseConnectionManager instance;
    private Connection connection;
    private final String databaseUrl;

    private DatabaseConnectionManager() {
        String databasePath = System.getProperty("stereovision.db.path", "stereo_vision.db");
        this.databaseUrl = "jdbc:sqlite:" + databasePath;
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("SQLite JDBC driver not found. Add sqlite-jdbc to the project classpath.", exception);
        }
    }

    public static synchronized DatabaseConnectionManager getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectionManager();
        }
        return instance;
    }

    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(databaseUrl);
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }
        }
        return connection;
    }
}
