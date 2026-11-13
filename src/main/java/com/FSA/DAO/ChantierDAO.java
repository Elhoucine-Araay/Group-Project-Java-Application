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
import org.mindrot.jbcrypt.BCrypt;
import java.text.Normalizer;
import java.util.Locale;

public class ChantierDAO {

    // the 3 columns of the Projects page: change the words here if your team uses others
    public static final String PLANIFIE = "PLANIFIE";
    public static final String EN_COURS = "EN_COURS";
    public static final String TERMINE  = "TERMINE";

    public record Material(String nom, double prevue, double utilisee, String unite) {}

    public record Details(BigDecimal budget, BigDecimal depenses,
                          List<String> membres, List<Material> materiaux) {}

    public static String emailPour(String prenom, String nom) {
        return nettoyer(prenom) + "." + nettoyer(nom) + "@cms.ma";
    }

    private static String nettoyer(String s) {
        String n = Normalizer.normalize(s.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")            // remove accents
                .replaceAll("[^A-Za-z0-9]", "")      // remove spaces, dashes, ...
                .toLowerCase(Locale.ROOT);
        return n;
    }

    /** Creates the user + chef_projet rows on the given connection and returns the new id. */
    private static int creerChef(Connection cn, String prenom, String nom, String motDePasse) throws SQLException {
        String sql = """
        INSERT INTO public.utilisateur (nom, email, "motDePasse")
        VALUES (?, ?, ?)
        RETURNING "idUtilisateur"
        """;
        int id;
        try (PreparedStatement st = cn.prepareStatement(sql)) {
            st.setString(1, prenom.trim() + " " + nom.trim());
            st.setString(2, emailPour(prenom, nom));
            st.setString(3, BCrypt.hashpw(motDePasse, BCrypt.gensalt()));
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                id = rs.getInt(1);
            }
        }
        try (PreparedStatement st = cn.prepareStatement(
                "INSERT INTO public.chef_projet (\"idUtilisateur\") VALUES (?)")) {
            st.setInt(1, id);
            st.executeUpdate();
        }
        return id;
    }
    public void assignerChef(int idChantier, String prenom, String nom, String motDePasse,
                             String statut) throws SQLException {
        String sql = """
        UPDATE public.chantier
        SET "idChefProjet" = ?, statut = ?
        WHERE "idChantier" = ? AND "idChefProjet" IS NULL
        """;
        try (Connection cn = DatabaseConnection.getConnection()) {
            cn.setAutoCommit(false);
            try {
                int idChef = creerChef(cn, prenom, nom, motDePasse);
                try (PreparedStatement st = cn.prepareStatement(sql)) {
                    st.setInt(1, idChef);
                    st.setString(2, statut);
                    st.setInt(3, idChantier);
                    if (st.executeUpdate() != 1) {
                        throw new SQLException("This project already has a chef.");
                    }
                }
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            }
        }
    }
    public record ChefOption(int id, String nom) {
        @Override public String toString() { return nom; }   // shown in the ComboBox
    }

    /** Project chefs who don't run a project yet. */
    public List<ChefOption> chefsDisponibles() throws SQLException {
        String sql = """
        SELECT u."idUtilisateur", u.nom
        FROM public.chef_projet cp
        JOIN public.utilisateur u ON u."idUtilisateur" = cp."idUtilisateur"
        WHERE NOT EXISTS (SELECT 1 FROM public.chantier c
                          WHERE c."idChefProjet" = cp."idUtilisateur")
        ORDER BY u.nom
        """;
        List<ChefOption> out = new ArrayList<>();
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement st = cn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) out.add(new ChefOption(rs.getInt(1), rs.getString(2)));
        }
        return out;
    }

    public void assignerChefExistant(int idChantier, int idChef, String statut) throws SQLException {
        String sql = """
        UPDATE public.chantier
        SET "idChefProjet" = ?, statut = ?
        WHERE "idChantier" = ? AND "idChefProjet" IS NULL
        """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement st = cn.prepareStatement(sql)) {
            st.setInt(1, idChef);
            st.setString(2, statut);
            st.setInt(3, idChantier);
            if (st.executeUpdate() != 1) {
                throw new SQLException("This project already has a chef.");
            }
        }
    }
    /** What the project pop-up shows. Loaded when a card is clicked. */
    public Details details(int idChantier) throws SQLException {
        BigDecimal budget = BigDecimal.ZERO;
        BigDecimal depenses = BigDecimal.ZERO;
        List<String> membres = new ArrayList<>();
        List<Material> materiaux = new ArrayList<>();

        try (Connection c = DatabaseConnection.getConnection()) {

            try (PreparedStatement st = c.prepareStatement(
                    "SELECT budget, depenses FROM public.chantier WHERE \"idChantier\" = ?")) {
                st.setInt(1, idChantier);
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) {
                        budget = rs.getBigDecimal("budget");
                        depenses = rs.getBigDecimal("depenses");
                    }
                }
            }

            // workers assigned to this site (DISTINCT: one worker can have several assignments)
            try (PreparedStatement st = c.prepareStatement("""
                SELECT DISTINCT o."idOuvrier", o.prenom, o.nom
                FROM public.affectation a
                JOIN public.ouvrier o ON o."idOuvrier" = a."idOuvrier"
                WHERE a."idChantier" = ?
                ORDER BY o.nom, o.prenom
                """)) {
                st.setInt(1, idChantier);
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        String prenom = rs.getString("prenom");
                        String nom = rs.getString("nom");
                        membres.add(((prenom == null ? "" : prenom) + " " + (nom == null ? "" : nom)).trim());
                    }
                }
            }

            // materials of this site: planned and used quantities, added up per material
            try (PreparedStatement st = c.prepareStatement("""
                SELECT m.nom, m.unite,
                       COALESCE(SUM(a."quantitePrevue"), 0)   AS prevue,
                       COALESCE(SUM(a."quantiteUtilisee"), 0) AS utilisee
                FROM public.affectation a
                JOIN public.materiel m ON m."idMateriel" = a."idMateriel"
                WHERE a."idChantier" = ?
                GROUP BY m."idMateriel", m.nom, m.unite
                ORDER BY m.nom
                """)) {
                st.setInt(1, idChantier);
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        materiaux.add(new Material(rs.getString("nom"), rs.getDouble("prevue"),
                                rs.getDouble("utilisee"), rs.getString("unite")));
                    }
                }
            }
        }
        return new Details(budget, depenses, membres, materiaux);
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


    public static void ajouter(String nom, String adresse, LocalDate debut, LocalDate fin, String statut,
                               String chefPrenom, String chefNom, String chefMotDePasse,
                               BigDecimal budget, String image) throws SQLException {

        String sql = """
        INSERT INTO public.chantier
            (nom, adresse, "dateDebut", "dateFinPrevue", statut,
             "idChefDepartement", "idChefProjet", budget, image)
        VALUES (?, ?, ?, ?, ?,
                (SELECT MIN("idUtilisateur") FROM public.chef_departement), ?, ?, ?)
        """;

        try (Connection cn = DatabaseConnection.getConnection()) {
            cn.setAutoCommit(false);                       // one transaction
            try {
                Integer idChef = null;
                if (chefPrenom != null) {
                    idChef = creerChef(cn, chefPrenom, chefNom, chefMotDePasse);
                }

                try (PreparedStatement st = cn.prepareStatement(sql)) {
                    st.setString(1, nom);
                    st.setString(2, adresse);
                    st.setDate(3, java.sql.Date.valueOf(debut));
                    st.setDate(4, java.sql.Date.valueOf(fin));
                    st.setString(5, statut);
                    if (idChef == null) st.setNull(6, Types.INTEGER); else st.setInt(6, idChef);
                    st.setBigDecimal(7, budget);
                    st.setString(8, image);
                    st.executeUpdate();
                }
                cn.commit();                               // both rows saved
            } catch (SQLException e) {
                cn.rollback();                             // nothing saved
                throw e;
            }
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