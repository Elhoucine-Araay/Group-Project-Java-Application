
package com.FSA.DAO;

import com.FSA.database.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UtilisateurDAO {

    private static final String SQL_LOGIN =
            "SELECT u.\"idUtilisateur\", u.nom, u.email, u.\"motDePasse\", " +
                    "CASE " +
                    "WHEN cd.\"idUtilisateur\" IS NOT NULL THEN 'CHEF_DEPARTEMENT' " +
                    "WHEN cp.\"idUtilisateur\" IS NOT NULL THEN 'CHEF_PROJET' " +
                    "END AS role " +
                    "FROM public.utilisateur u " +
                    "LEFT JOIN public.chef_departement cd " +
                    "ON u.\"idUtilisateur\" = cd.\"idUtilisateur\" " +
                    "LEFT JOIN public.chef_projet cp " +
                    "ON u.\"idUtilisateur\" = cp.\"idUtilisateur\" " +
                    "WHERE u.email = ?";

    public String authentifier(String email, String password) {

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_LOGIN)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    String hashedPassword = rs.getString("motDePasse");

                    if (BCrypt.checkpw(password, hashedPassword)) {
                        return rs.getString("role");
                    }
                }
            }

        } catch (SQLException | IllegalArgumentException e) {
            e.printStackTrace();
        }

        return null;
    }
}
