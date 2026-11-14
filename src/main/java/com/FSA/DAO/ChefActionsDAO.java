package com.FSA.DAO;

import com.FSA.database.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChefActionsDAO {

    public record MaterielOption(int id, String nom, String unite) {
        @Override public String toString() {
            return unite == null || unite.isBlank() ? nom : nom + " (" + unite + ")";
        }
    }
    public record Request(String texte, String statut) {}

    // =================== attendance ===================

    /** Adds a worker to the project and records today's attendance. */
    public void ajouterOuvrier(int idChantier, String nomComplet, String statut) throws SQLException {
        String[] parts = nomComplet.trim().split("\\s+", 2);
        String prenom = parts.length > 1 ? parts[0] : null;      // "Ali Karim" -> Ali / Karim
        String nom = parts.length > 1 ? parts[1] : parts[0];

        try (Connection cn = DatabaseConnection.getConnection()) {
            cn.setAutoCommit(false);
            try {
                int idOuvrier;
                try (PreparedStatement st = cn.prepareStatement(
                        "INSERT INTO public.ouvrier (nom, prenom) VALUES (?, ?) RETURNING \"idOuvrier\"")) {
                    st.setString(1, nom);
                    st.setString(2, prenom);
                    try (ResultSet rs = st.executeQuery()) { rs.next(); idOuvrier = rs.getInt(1); }
                }
                affecter(cn, idChantier, tacheGenerale(cn, idChantier), "OUVRIER", idOuvrier, null);
                ecrirePresence(cn, idOuvrier, statut);
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    /** Changes today's status of a worker (creates the row if there is none yet). */
    public void changerPresence(int idOuvrier, String statut) throws SQLException {
        try (Connection cn = DatabaseConnection.getConnection()) {
            ecrirePresence(cn, idOuvrier, statut);
        }
    }

    private static void ecrirePresence(Connection cn, int idOuvrier, String label) throws SQLException {
        String db = switch (label) {
            case "Present" -> "PRESENT";
            case "Late"    -> "RETARD";
            default        -> "ABSENT";
        };
        String heure = db.equals("ABSENT") ? null
                : LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        int rows;
        try (PreparedStatement st = cn.prepareStatement("""
                UPDATE public.presence SET statut = ?, "heureArrivee" = ?
                WHERE "idOuvrier" = ? AND date = CURRENT_DATE
                """)) {
            st.setString(1, db);
            st.setString(2, heure);
            st.setInt(3, idOuvrier);
            rows = st.executeUpdate();
        }
        if (rows == 0) {
            try (PreparedStatement st = cn.prepareStatement("""
                    INSERT INTO public.presence (date, "heureArrivee", statut, "idOuvrier")
                    VALUES (CURRENT_DATE, ?, ?, ?)
                    """)) {
                st.setString(1, heure);
                st.setString(2, db);
                st.setInt(3, idOuvrier);
                st.executeUpdate();
            }
        }
    }

    // =================== equipment ===================

    public void ajouterMachine(int idChantier, String nom, String type, String statut) throws SQLException {
        try (Connection cn = DatabaseConnection.getConnection()) {
            cn.setAutoCommit(false);
            try {
                int idMachine;
                try (PreparedStatement st = cn.prepareStatement("""
                        INSERT INTO public.machine (nom, type, etat, disponible)
                        VALUES (?, ?, ?, ?) RETURNING "idMachine"
                        """)) {
                    st.setString(1, nom);
                    st.setString(2, type);
                    st.setString(3, etatDb(statut));
                    st.setBoolean(4, statut.equals("Operational"));
                    try (ResultSet rs = st.executeQuery()) { rs.next(); idMachine = rs.getInt(1); }
                }
                affecter(cn, idChantier, tacheGenerale(cn, idChantier), "MACHINE", null, idMachine);
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    public void changerEtatMachine(int idMachine, String statut) throws SQLException {
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement st = cn.prepareStatement(
                     "UPDATE public.machine SET etat = ?, disponible = ? WHERE \"idMachine\" = ?")) {
            st.setString(1, etatDb(statut));
            st.setBoolean(2, statut.equals("Operational"));
            st.setInt(3, idMachine);
            st.executeUpdate();
        }
    }

    private static String etatDb(String label) {
        return switch (label) {
            case "Operational" -> "OPERATIONNELLE";
            case "Maintenance" -> "EN_MAINTENANCE";
            case "Broken"      -> "EN_PANNE";
            default            -> "INACTIVE";                // Idle
        };
    }

    // =================== requests ===================

    public void demanderBudget(int idChantier, BigDecimal montant, String motif) throws SQLException {
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement st = cn.prepareStatement("""
                     INSERT INTO public.demande (type, "idChantier", montant, motif)
                     VALUES ('BUDGET', ?, ?, ?)
                     """)) {
            st.setInt(1, idChantier);
            st.setBigDecimal(2, montant);
            st.setString(3, motif);
            st.executeUpdate();
        }
    }

    public void demanderMateriel(int idChantier, int idMateriel, double quantite, String motif)
            throws SQLException {
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement st = cn.prepareStatement("""
                     INSERT INTO public.demande (type, "idChantier", "idMateriel", quantite, motif)
                     VALUES ('MATERIEL', ?, ?, ?, ?)
                     """)) {
            st.setInt(1, idChantier);
            st.setInt(2, idMateriel);
            st.setDouble(3, quantite);
            st.setString(4, motif);
            st.executeUpdate();
        }
    }

    /** All the materials of the catalogue (table materiel), to choose from in a request. */
    public List<MaterielOption> materielsCatalogue() throws SQLException {
        List<MaterielOption> out = new ArrayList<>();
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement st = cn.prepareStatement(
                     "SELECT \"idMateriel\", nom, unite FROM public.materiel ORDER BY nom");
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) out.add(new MaterielOption(rs.getInt(1), rs.getString(2), rs.getString(3)));
        }
        return out;
    }

    /** The last 20 requests of the project, for "BUDGET" or "MATERIEL". */
    public List<Request> demandes(int idChantier, String type) throws SQLException {
        String sql = """
            SELECT d.montant, d.quantite, d.motif, d.statut, m.nom, m.unite
            FROM public.demande d
            LEFT JOIN public.materiel m ON m."idMateriel" = d."idMateriel"
            WHERE d."idChantier" = ? AND d.type = ?
            ORDER BY d."idDemande" DESC
            LIMIT 20
            """;
        List<Request> out = new ArrayList<>();
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement st = cn.prepareStatement(sql)) {
            st.setInt(1, idChantier);
            st.setString(2, type);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    String motif = rs.getString(3) == null ? "" : " - " + rs.getString(3);
                    String texte = type.equals("BUDGET")
                            ? String.format(Locale.US, "%,.0f DH", rs.getDouble(1)) + motif
                            : nombre(rs.getDouble(2)) + " " + (rs.getString(6) == null ? "" : rs.getString(6))
                            + " " + rs.getString(5) + motif;
                    String statut = switch (rs.getString(4)) {
                        case "APPROUVEE" -> "Approved";
                        case "REFUSEE"   -> "Rejected";
                        default          -> "Pending";
                    };
                    out.add(new Request(texte.replace("  ", " ").trim(), statut));
                }
            }
        }
        return out;
    }

    private static String nombre(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    // =================== shared helpers ===================

    /** The default task "General" of the project (created the first time). */
    private static int tacheGenerale(Connection cn, int idChantier) throws SQLException {
        try (PreparedStatement st = cn.prepareStatement(
                "SELECT \"idTache\" FROM public.tache WHERE \"idChantier\" = ? AND nom = 'General' LIMIT 1")) {
            st.setInt(1, idChantier);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        try (PreparedStatement st = cn.prepareStatement("""
                INSERT INTO public.tache (nom, description, statut, "pourcentageAvancement", "idChantier")
                VALUES ('General', 'Default task for the resources of the project', 'EN_COURS', 0, ?)
                RETURNING "idTache"
                """)) {
            st.setInt(1, idChantier);
            try (ResultSet rs = st.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }

    private static void affecter(Connection cn, int idChantier, int idTache, String type,
                                 Integer idOuvrier, Integer idMachine) throws SQLException {
        try (PreparedStatement st = cn.prepareStatement("""
                INSERT INTO public.affectation
                    ("dateAffectation", "typeRessource", statut, "idChantier", "idTache", "idMachine", "idOuvrier")
                VALUES (CURRENT_DATE, ?, 'ACTIVE', ?, ?, ?, ?)
                """)) {
            st.setString(1, type);
            st.setInt(2, idChantier);
            st.setInt(3, idTache);
            if (idMachine == null) st.setNull(4, Types.INTEGER); else st.setInt(4, idMachine);
            if (idOuvrier == null) st.setNull(5, Types.INTEGER); else st.setInt(5, idOuvrier);
            st.executeUpdate();
        }
    }
}