package com.FSA.dao;

import com.FSA.database.DatabaseConnection;
import com.FSA.model.Chantier;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Data-access layer for the PostgreSQL table public.chantier. */
public class ChantierDAO {

    private static final String INSERT_SQL = """
            INSERT INTO public.chantier
                (nom, adresse, \"dateDebut\", \"dateFinPrevue\", statut,
                 avancement, \"idChefDepartement\", \"idChefProjet\")
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING \"idChantier\"
            """;

    private static final String SELECT_ALL_SQL = """
            SELECT \"idChantier\", nom, adresse, \"dateDebut\", \"dateFinPrevue\",
                   statut, avancement, \"idChefDepartement\", \"idChefProjet\"
            FROM public.chantier
            ORDER BY \"idChantier\"
            """;

    private static final String SELECT_BY_ID_SQL = SELECT_ALL_SQL
            .replace(" ORDER BY \"idChantier\"", " WHERE \"idChantier\" = ?");

    private static final String UPDATE_SQL = """
            UPDATE public.chantier
            SET nom = ?, adresse = ?, \"dateDebut\" = ?, \"dateFinPrevue\" = ?,
                statut = ?, avancement = ?, \"idChefDepartement\" = ?, \"idChefProjet\" = ?
            WHERE \"idChantier\" = ?
            """;

    private static final String DELETE_SQL =
            "DELETE FROM public.chantier WHERE \"idChantier\" = ?";

    public int create(Chantier chantier) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            bindFields(statement, chantier);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("La création du chantier n'a retourné aucun identifiant.");
                }
                return result.getInt("idChantier");
            }
        }
    }

    public List<Chantier> findAll() throws SQLException {
        List<Chantier> chantiers = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                chantiers.add(mapRow(result));
            }
        }
        return chantiers;
    }

    public Optional<Chantier> findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID_SQL)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapRow(result)) : Optional.empty();
            }
        }
    }

    public boolean update(Chantier chantier) throws SQLException {
        String sql = UPDATE_SQL;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindFields(statement, chantier);
            statement.setInt(9, chantier.getIdChantier());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean deleteById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private void bindFields(PreparedStatement statement, Chantier chantier) throws SQLException {
        statement.setString(1, chantier.getNom());
        statement.setString(2, chantier.getAdresse());
        statement.setDate(3, chantier.getDateDebut() == null ? null : Date.valueOf(chantier.getDateDebut()));
        statement.setDate(4, chantier.getDateFinPrevue() == null ? null : Date.valueOf(chantier.getDateFinPrevue()));
        statement.setString(5, chantier.getStatut());
        statement.setDouble(6, chantier.getAvancement());
        statement.setInt(7, chantier.getIdChefDepartement());
        statement.setInt(8, chantier.getIdChefProjet());
    }

    private Chantier mapRow(ResultSet result) throws SQLException {
        Chantier chantier = new Chantier();
        chantier.setIdChantier(result.getInt("idChantier"));
        chantier.setNom(result.getString("nom"));
        chantier.setAdresse(result.getString("adresse"));
        Date dateDebut = result.getDate("dateDebut");
        Date dateFin = result.getDate("dateFinPrevue");
        chantier.setDateDebut(dateDebut == null ? null : dateDebut.toLocalDate());
        chantier.setDateFinPrevue(dateFin == null ? null : dateFin.toLocalDate());
        chantier.setStatut(result.getString("statut"));
        chantier.setAvancement(result.getDouble("avancement"));
        chantier.setIdChefDepartement(result.getInt("idChefDepartement"));
        chantier.setIdChefProjet(result.getInt("idChefProjet"));
        return chantier;
    }
}
