package com.mescode.japanese.database;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class DatabaseInitializer {

    public static void initialize() {
        if (!DatabaseConnection.isDbEnabled()) {
            System.out.println("Database is disabled. Application will run with JSON-based storage.");
            return;
        }

        try {
            // Read SQL file content
            String resource = "sql/create_tables.sql";
            InputStream inputStream = DatabaseInitializer.class.getClassLoader().getResourceAsStream(resource);
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resource);
            }
            String sql = new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining("\n"));

            // Execute SQL
            DatabaseConnection.executeSql(sql);
            System.out.println("Database initialization completed successfully.");
        } catch (SQLException e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("login failed") || message.contains("cannot open database")) {
                System.out.println("Warning: Database login failed or database does not exist.");
                System.out.println("Please ensure:");
                System.out.println("1. MS SQL Server is installed and running on localhost:1433");
                System.out.println("2. The database 'JapaneseAlphabetQuiz' exists (or create it manually)");
                System.out.println("3. The username 'sa' and password in application.properties are correct");
                System.out.println("4. SQL Server Authentication is enabled in SQL Server configuration");
                System.out.println("Application will continue with JSON-based storage.");
            } else if (message.contains("integrated authentication")) {
                System.out.println("Warning: Integrated authentication is not supported. Please use SQL Server Authentication.");
                System.out.println("Update the 'db.url' in application.properties to use 'user' and 'password' parameters.");
                System.out.println("Application will continue with JSON-based storage.");
            } else {
                System.err.println("Database initialization failed: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Error during database initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
