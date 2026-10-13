
package com.groupapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/group_project";

    private static final String USER = "group_app";
    private static final String PASSWORD = "local_dev_password";

    // Connect to PostgreSQL
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Create the products table in PostgreSQL
    public static void createProductsTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS products (
                id SERIAL PRIMARY KEY,
                barcode VARCHAR(100) UNIQUE NOT NULL,
                name VARCHAR(150) NOT NULL,
                quantity INTEGER NOT NULL DEFAULT 0,
                purchase_price NUMERIC(10, 2) NOT NULL DEFAULT 0,
                selling_price NUMERIC(10, 2) NOT NULL DEFAULT 0
            )
            """;

        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(sql);
            System.out.println("Products table created successfully!");

        } catch (SQLException e) {
            System.err.println("Failed to create products table.");
            e.printStackTrace();
        }
    }

    // This is the main method
    public static void main(String[] args) {
        System.out.println("Starting database test...");

        try (Connection connection = connect()) {
            System.out.println("Connected to PostgreSQL successfully!");
            System.out.println("Database: " + connection.getCatalog());

        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            e.printStackTrace();
            return;
        }

        System.out.println("Calling createProductsTable()...");
        createProductsTable();
        System.out.println("Database test finished.");
    }
}