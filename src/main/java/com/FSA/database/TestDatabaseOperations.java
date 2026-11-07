
package com.FSA.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestDatabaseOperations {

    public static void main(String[] args) {

        String insertion = """
            INSERT INTO materiel
            (nom, description, "quantiteStock", unite, "seuilMinimum")
            VALUES (?, ?, ?, ?, ?)
            RETURNING "idMateriel"
            """;

        String lecture = """
            SELECT "idMateriel", nom, "quantiteStock", unite
            FROM materiel
            WHERE "idMateriel" = ?
            """;

        try (Connection connexion = DatabaseConnection.getConnection()) {

            // 1. Insérer un matériel fictif
            int idMateriel;

            try (PreparedStatement requete =
                         connexion.prepareStatement(insertion)) {

                requete.setString(1, "Ciment test");
                requete.setString(2, "Matériel de test");
                requete.setDouble(3, 50.0);
                requete.setString(4, "sacs");
                requete.setDouble(5, 10.0);

                try (ResultSet resultat = requete.executeQuery()) {
                    resultat.next();
                    idMateriel = resultat.getInt("idMateriel");
                }
            }

            System.out.println("INSERT réussi !");
            System.out.println("Matériel ajouté : " + idMateriel);

            // 2. Récupérer ce matériel
            try (PreparedStatement requete =
                         connexion.prepareStatement(lecture)) {

                requete.setInt(1, idMateriel);

                try (ResultSet resultat = requete.executeQuery()) {

                    if (resultat.next()) {
                        System.out.println("SELECT réussi !");
                        System.out.println(
                                "Nom : " + resultat.getString("nom")
                        );
                        System.out.println(
                                "Quantité : " +
                                        resultat.getDouble("quantiteStock")
                        );
                        System.out.println(
                                "Unité : " + resultat.getString("unite")
                        );
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Erreur SQL : " + e.getMessage());
        }
    }
}
