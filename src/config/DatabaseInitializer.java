package stereovision.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {
    private static boolean initialized = false;

    private DatabaseInitializer() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        try {
            Connection connection = DatabaseConnectionManager.getInstance().getConnection();
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS stereo_projects (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL,
                            description TEXT NOT NULL
                        )
                        """);

                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS stereo_image_pairs (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            project_id INTEGER NOT NULL,
                            label TEXT NOT NULL,
                            left_image_path TEXT NOT NULL,
                            right_image_path TEXT NOT NULL,
                            image_width INTEGER NOT NULL,
                            image_height INTEGER NOT NULL,
                            FOREIGN KEY(project_id) REFERENCES stereo_projects(id) ON DELETE CASCADE
                        )
                        """);

                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS camera_parameters (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            project_id INTEGER NOT NULL UNIQUE,
                            fx REAL NOT NULL,
                            fy REAL NOT NULL,
                            cx REAL NOT NULL,
                            cy REAL NOT NULL,
                            baseline REAL NOT NULL,
                            approximate INTEGER NOT NULL,
                            FOREIGN KEY(project_id) REFERENCES stereo_projects(id) ON DELETE CASCADE
                        )
                        """);

                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS reconstruction_sessions (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            project_id INTEGER NOT NULL,
                            pair_id INTEGER NOT NULL,
                            algorithm_name TEXT NOT NULL,
                            disparity_output_path TEXT,
                            disparity_heatmap_output_path TEXT,
                            invalid_disparity_mask_output_path TEXT,
                            depth_output_path TEXT,
                            depth_heatmap_output_path TEXT,
                            points_output_path TEXT,
                            total_pixels INTEGER NOT NULL,
                            valid_depth_pixels INTEGER NOT NULL,
                            point_count INTEGER NOT NULL,
                            min_depth REAL NOT NULL,
                            max_depth REAL NOT NULL,
                            mean_depth REAL NOT NULL,
                            created_at TEXT NOT NULL,
                            FOREIGN KEY(project_id) REFERENCES stereo_projects(id) ON DELETE CASCADE,
                            FOREIGN KEY(pair_id) REFERENCES stereo_image_pairs(id) ON DELETE CASCADE
                        )
                        """);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not initialize the database schema.", exception);
        }

        initialized = true;
    }
}
