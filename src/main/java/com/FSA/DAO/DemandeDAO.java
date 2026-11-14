package com.FSA.DAO;

import com.FSA.database.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Requests of the project chefs, seen by the chef de département. */
public class DemandeDAO {

    public record Row(int id, String type, String projet, String chef, String texte,
                      String motif, String contexte, LocalDateTime date) {}

    /** statut: EN_ATTENTE, APPROUVEE or REFUSEE. */
    public List<Row> liste(String statut) throws SQLException {
        String sql = """
            SELECT d."idDemande", d.type, d.montant, d.quantite, d.motif, d."dateDemande",
                   c.nom, COALESCE(c.budget, 0), u.nom,
                   m.nom, m.unite, m."quantiteStock"
            FROM public.demande d
            JOIN public.chantier c ON c."idChantier" = d."idChantier"
            LEFT JOIN public.utilisateur u ON u."idUtilisateur" = c."idChefProjet"
            LEFT JOIN public.materiel m ON m."idMateriel" = d."idMateriel"
            WHERE d.statut = ?
            ORDER BY d."dateDemande" DESC
            """;
        List<Row> out = new ArrayList<>();
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement st = cn.prepareStatement(sql)) {
            st.setString(1, statut);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString(2);
                    String texte, contexte;
                    if (type.equals("BUDGET")) {
                        double montant = rs.getDouble(3), budget = rs.getDouble(8);
                        texte = "+" + money(montant);
                        contexte = "Budget now " + money(budget) + "  →  " + money(budget + montant) + " if approved";
                    } else {
                        String unite = rs.getString(11) == null ? "" : " " + rs.getString(11);
                        texte = nombre(rs.getDouble(4)) + unite + " of " + rs.getString(10);
                        contexte = rs.getObject(12) == null ? "Stock unknown"
                                : "In stock: " + nombre(rs.getDouble(12)) + unite;
                    }
                    out.add(new Row(rs.getInt(1), type, rs.getString(7), rs.getString(9), texte,
                            rs.getString(5) == null ? "" : rs.getString(5), contexte,
                            rs.getTimestamp(6).toLocalDateTime()));
                }
            }
        }
        return out;
    }

    /** Approves the request and applies it to the project, in one transaction. */
    public void approuver(int idDemande) throws SQLException {
        try (Connection cn = DatabaseConnection.getConnection()) {
            cn.setAutoCommit(false);
            try {
                String type;
                int idChantier;
                Integer idMateriel;
                BigDecimal montant;
                double quantite;

                try (PreparedStatement st = cn.prepareStatement("""
                        SELECT type, "idChantier", "idMateriel", montant, quantite, statut
                        FROM public.demande WHERE "idDemande" = ? FOR UPDATE
                        """)) {
                    st.setInt(1, idDemande);
                    try (ResultSet rs = st.executeQuery()) {
                        if (!rs.next() || !"EN_ATTENTE".equals(rs.getString(6))) {
                            throw new SQLException("This request was already handled.");
                        }
                        type = rs.getString(1);
                        idChantier = rs.getInt(2);
                        int m = rs.getInt(3);
                        idMateriel = rs.wasNull() ? null : m;
                        montant = rs.getBigDecimal(4);
                        quantite = rs.getDouble(5);
                    }
                }

                if (type.equals("BUDGET")) {
                    try (PreparedStatement st = cn.prepareStatement("""
                            UPDATE public.chantier SET budget = COALESCE(budget, 0) + ?
                            WHERE "idChantier" = ?
                            """)) {
                        st.setBigDecimal(1, montant);
                        st.setInt(2, idChantier);
                        st.executeUpdate();
                    }
                } else {
                    int rows;
                    try (PreparedStatement st = cn.prepareStatement("""
                            UPDATE public.affectation
                            SET "quantitePrevue" = COALESCE("quantitePrevue", 0) + ?
                            WHERE "idAffectation" = (SELECT MIN("idAffectation") FROM public.affectation
                                                     WHERE "idChantier" = ? AND "idMateriel" = ?)
                            """)) {
                        st.setDouble(1, quantite);
                        st.setInt(2, idChantier);
                        st.setInt(3, idMateriel);
                        rows = st.executeUpdate();
                    }
                    if (rows == 0) {                       // the project doesn't use this material yet
                        try (PreparedStatement st = cn.prepareStatement("""
                                INSERT INTO public.affectation
                                    ("dateAffectation", "typeRessource", statut, "idChantier", "idTache",
                                     "idMateriel", "quantitePrevue", "quantiteUtilisee")
                                VALUES (CURRENT_DATE, 'MATERIEL', 'ACTIVE', ?, ?, ?, ?, 0)
                                """)) {
                            st.setInt(1, idChantier);
                            st.setInt(2, ChefActionsDAO.tacheGenerale(cn, idChantier));
                            st.setInt(3, idMateriel);
                            st.setDouble(4, quantite);
                            st.executeUpdate();
                        }
                    }
                }

                marquer(cn, idDemande, "APPROUVEE");
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    public void refuser(int idDemande) throws SQLException {
        try (Connection cn = DatabaseConnection.getConnection()) {
            marquer(cn, idDemande, "REFUSEE");
        }
    }

    private static void marquer(Connection cn, int idDemande, String statut) throws SQLException {
        try (PreparedStatement st = cn.prepareStatement(
                "UPDATE public.demande SET statut = ? WHERE \"idDemande\" = ? AND statut = 'EN_ATTENTE'")) {
            st.setString(1, statut);
            st.setInt(2, idDemande);
            if (st.executeUpdate() != 1) throw new SQLException("This request was already handled.");
        }
    }

    private static String money(double v) { return String.format(Locale.US, "%,.0f DH", v); }
    private static String nombre(double v) { return v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v); }
}