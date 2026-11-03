
package com.FSA.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/chantier_db";

    private static final String USER = "postgres";

    public static Connection getConnection() throws SQLException {

        String password = System.getenv("CHANTIER_DB_PASSWORD");

        if (password == null || password.isBlank()) {
            throw new SQLException(
                    "Mot de passe PostgreSQL non configuré."
            );
        }

        return DriverManager.getConnection(URL, USER, password);
    }

    public static void main(String[] args) {

        try (Connection connexion = getConnection()) {

            System.out.println("Connexion PostgreSQL réussie !");
            System.out.println("Base : " + connexion.getCatalog());

        } catch (SQLException e) {

            System.out.println("Erreur : " + e.getMessage());
        }
    }
}
