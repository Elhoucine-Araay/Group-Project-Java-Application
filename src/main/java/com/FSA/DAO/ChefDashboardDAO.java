package com.FSA.DAO;

import com.FSA.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChefDashboardDAO {

    public record Project(int id, String nom, double budget, double depenses) {}
    public record Worker(int id, String nom, String statut) {}
    public record Equipment(int id, String nom, String statut) {}
    public record Material(String nom, double utilisee, double prevue, String unite) {}
    public record Data(Project projet, List<Worker> ouvriers,
                       List<Equipment> machines, List<Material> materiaux) {}

    /** Everything the Dashboard page shows, or null if this chef has no project yet. */
    public Data pourChef(int idChef) throws SQLException {
        try (Connection cn = DatabaseConnection.getConnection()) {
            Project p = projet(cn, idChef);
            if (p == null) return null;
            return new Data(p, ouvriers(cn, p.id()), machines(cn, p.id()), materiaux(cn, p.id()));
        }
    }

    private Project projet(Connection cn, int idChef) throws SQLException {
        String sql = """
            SELECT "idChantier", nom, COALESCE(budget, 0), COALESCE(depenses, 0)
            FROM public.chantier WHERE "idChefProjet" = ?
            """;
        try (PreparedStatement st = cn.prepareStatement(sql)) {
            st.setInt(1, idChef);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next()
                        ? new Project(rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getDouble(4))
                        : null;
            }
        }
    }

    /** Workers of the project + their attendance of TODAY (no record = Absent). */
    private List<Worker> ouvriers(Connection cn, int idChantier) throws SQLException {
        String sql = """
                SELECT DISTINCT ON (o."idOuvrier") o."idOuvrier", o.prenom, o.nom, p.statut
                FROM public.affectation a
                JOIN public.ouvrier o ON o."idOuvrier" = a."idOuvrier"
                LEFT JOIN public.presence p
                       ON p."idOuvrier" = o."idOuvrier" AND p.date = CURRENT_DATE
                WHERE a."idChantier" = ?
                ORDER BY o."idOuvrier", p."idPresence" DESC
                """;
        List<Worker> out = new ArrayList<>();
        try (PreparedStatement st = cn.prepareStatement(sql)) {
            st.setInt(1, idChantier);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    String full = ((rs.getString(2) == null ? "" : rs.getString(2)) + " "
                            + (rs.getString(3) == null ? "" : rs.getString(3))).trim();
                    out.add(new Worker(rs.getInt(1), full, presence(rs.getString(4))));
                }
            }
        }
        return out;
    }

    private List<Equipment> machines(Connection cn, int idChantier) throws SQLException {
        String sql = """
            SELECT DISTINCT m."idMachine", m.nom, m.etat
            FROM public.affectation a
            JOIN public.machine m ON m."idMachine" = a."idMachine"
            WHERE a."idChantier" = ?
            ORDER BY m."idMachine"
            """;
        List<Equipment> out = new ArrayList<>();
        try (PreparedStatement st = cn.prepareStatement(sql)) {
            st.setInt(1, idChantier);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) out.add(new Equipment(rs.getInt(1), rs.getString(2), etat(rs.getString(3))));
            }
        }
        return out;
    }

    private List<Material> materiaux(Connection cn, int idChantier) throws SQLException {
        String sql = """
            SELECT m.nom,
                   COALESCE(SUM(a."quantiteUtilisee"), 0),
                   COALESCE(SUM(a."quantitePrevue"), 0),
                   m.unite
            FROM public.affectation a
            JOIN public.materiel m ON m."idMateriel" = a."idMateriel"
            WHERE a."idChantier" = ?
            GROUP BY m."idMateriel", m.nom, m.unite
            ORDER BY m.nom
            """;
        List<Material> out = new ArrayList<>();
        try (PreparedStatement st = cn.prepareStatement(sql)) {
            st.setInt(1, idChantier);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) out.add(new Material(rs.getString(1), rs.getDouble(2),
                        rs.getDouble(3), rs.getString(4) == null ? "" : rs.getString(4)));
            }
        }
        return out;
    }

    // DB words -> the words the page (and its CSS dots) know
    private static String presence(String s) {
        if (s == null) return "Absent";
        String u = s.toUpperCase(Locale.ROOT);
        if (u.contains("RET") || u.contains("LATE")) return "Late";
        if (u.contains("PRES")) return "Present";
        return "Absent";
    }

    private static String etat(String s) {
        if (s == null) return "Idle";
        String u = s.toUpperCase(Locale.ROOT);
        if (u.contains("PANNE")) return "Broken";
        if (u.contains("MAINT")) return "Maintenance";
        if (u.contains("OPERATION")) return "Operational";
        return "Idle";
    }
}