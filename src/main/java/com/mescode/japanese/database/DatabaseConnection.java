package com.mescode.japanese.database;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static String DB_URL;
    private static boolean DB_ENABLED = true;

    static {
        loadConfiguration();
    }

    private static void loadConfiguration() {
        try {
            InputStream inputStream = DatabaseConnection.class.getClassLoader()
                    .getResourceAsStream("application.properties");
            if (inputStream != null) {
                Properties props = new Properties();
                props.load(new BufferedReader(new InputStreamReader(inputStream)));
                DB_URL = props.getProperty("db.url", 
                    "jdbc:sqlserver://localhost:1433;databaseName=JapaneseAlphabetQuiz;integratedSecurity=true;encrypt=true;trustServerCertificate=true;");
                DB_ENABLED = Boolean.parseBoolean(props.getProperty("db.enabled", "true"));
            } else {
                DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=JapaneseAlphabetQuiz;integratedSecurity=true;encrypt=true;trustServerCertificate=true;";
                DB_ENABLED = true;
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load database configuration. Using defaults.");
            DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=JapaneseAlphabetQuiz;integratedSecurity=true;encrypt=true;trustServerCertificate=true;";
            DB_ENABLED = true;
        }
    }

    public static Connection getConnection() throws SQLException {
        if (!DB_ENABLED) {
            throw new SQLException("Database is disabled in configuration");
        }
        return DriverManager.getConnection(DB_URL);
    }

    public static void executeSql(String sql) throws SQLException {
        if (!DB_ENABLED) {
            throw new SQLException("Database is disabled in configuration");
        }
        try (Connection connection = getConnection();
             java.sql.Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isDbEnabled() {
        return DB_ENABLED;
    }
}
