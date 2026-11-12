package com.FSA.DAO;

import com.FSA.database.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.sql.Types;

public class ChantierDAO {

    // the 3 columns of the Projects page: change the words here if your team uses others
    public static final String PLANIFIE = "PLANIFIE";
    public static final String EN_COURS = "EN_COURS";
    public static final String TERMINE  = "TERMINE";

    /** An item of the "Chef de projet" list. ComboBox displays toString(). */
    public record ChefOption(int id, String nom) {
        @Override public String toString() { return nom; }
    }

    /** What one project card needs. */
    public record Card(int id, String nom, String adresse, LocalDate debut, LocalDate fin,
                       String statut, String image, String chef, int membres) {}

    public static String statutPour(LocalDate debut, LocalDate fin) {
        LocalDate today = LocalDate.now();
        if (today.isBefore(debut)) return PLANIFIE;
        if (today.isAfter(fin))    return TERMINE;
        return EN_COURS;
    }

    /** Chefs de projet who do not run a project yet (uq_chantier_chef_projet). */
    public List<ChefOption> chefsDisponibles() throws SQLException {
        String sql = """
                SELECT u."idUtilisateur", u.nom
                FROM public.chef_projet cp
                JOIN public.utilisateur u ON u."idUtilisateur" = cp."idUtilisateur"
                WHERE NOT EXISTS (SELECT 1 FROM public.chantier c
                                  WHERE c."idChefProjet" = cp."idUtilisateur")
                ORDER BY u.nom
                """;
        List<ChefOption> result = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement st = c.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                result.add(new ChefOption(rs.getInt("idUtilisateur"), rs.getString("nom")));
            }
        }
        return result;
    }

    public void ajouter(String nom, String adresse, LocalDate debut, LocalDate fin, String statut,
                        Integer idChefProjet, BigDecimal budget, String image) throws SQLException {
        String sql = """
            INSERT INTO public.chantier
                (nom, adresse, "dateDebut", "dateFinPrevue", statut, avancement,
                 "idChefDepartement", "idChefProjet", budget, image)
            VALUES (?, ?, ?, ?, ?, 0,
                    (SELECT MIN("idUtilisateur") FROM public.chef_departement),
                    ?, ?, ?)
            """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, nom);
            st.setString(2, adresse);
            st.setDate(3, Date.valueOf(debut));
            st.setDate(4, Date.valueOf(fin));
            st.setString(5, statut);
            if (idChefProjet == null) st.setNull(6, Types.INTEGER);
            else                      st.setInt(6, idChefProjet);
            st.setBigDecimal(7, budget);
            st.setString(8, image);
            st.executeUpdate();
        }

    }

    /** All projects, for the cards on the page. */
    public List<Card> toutes() throws SQLException {
        String sql = """
                SELECT c."idChantier", c.nom, c.adresse, c."dateDebut", c."dateFinPrevue",
                       c.statut, c.image, u.nom AS chef,
                       (SELECT COUNT(DISTINCT a."idOuvrier") FROM public.affectation a
                        WHERE a."idChantier" = c."idChantier") AS membres
                FROM public.chantier c
                LEFT JOIN public.utilisateur u ON u."idUtilisateur" = c."idChefProjet"
                ORDER BY c."dateDebut"
                """;
        List<Card> result = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement st = c.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                result.add(new Card(
                        rs.getInt("idChantier"),
                        rs.getString("nom"),
                        rs.getString("adresse"),
                        toLocal(rs.getDate("dateDebut")),
                        toLocal(rs.getDate("dateFinPrevue")),
                        rs.getString("statut"),
                        rs.getString("image"),
                        rs.getString("chef"),
                        rs.getInt("membres")));
            }
        }
        return result;
    }

    private static LocalDate toLocal(Date d) { return d == null ? null : d.toLocalDate(); }
}