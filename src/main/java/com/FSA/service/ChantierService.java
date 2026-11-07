package com.FSA.service;

import com.FSA.dao.ChantierDAO;
import com.FSA.model.Chantier;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Business rules for chantier operations. */
public class ChantierService {
    private final ChantierDAO chantierDAO;

    public ChantierService() {
        this(new ChantierDAO());
    }

    public ChantierService(ChantierDAO chantierDAO) {
        this.chantierDAO = chantierDAO;
    }

    public int create(Chantier chantier) throws SQLException {
        validate(chantier);
        return chantierDAO.create(chantier);
    }

    public List<Chantier> findAll() throws SQLException {
        return chantierDAO.findAll();
    }

    public Optional<Chantier> findById(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("L'identifiant doit être positif.");
        return chantierDAO.findById(id);
    }

    public boolean update(Chantier chantier) throws SQLException {
        if (chantier.getIdChantier() <= 0) {
            throw new IllegalArgumentException("Sélectionnez un chantier valide à modifier.");
        }
        validate(chantier);
        return chantierDAO.update(chantier);
    }

    public boolean delete(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("Sélectionnez un chantier valide à supprimer.");
        return chantierDAO.deleteById(id);
    }

    private void validate(Chantier chantier) {
        if (chantier == null) throw new IllegalArgumentException("Le chantier est obligatoire.");
        if (isBlank(chantier.getNom())) throw new IllegalArgumentException("Le nom du chantier est obligatoire.");
        if (isBlank(chantier.getAdresse())) throw new IllegalArgumentException("L'adresse du chantier est obligatoire.");
        if (chantier.getDateDebut() == null) throw new IllegalArgumentException("La date de début est obligatoire.");
        if (chantier.getDateFinPrevue() == null) throw new IllegalArgumentException("La date de fin prévue est obligatoire.");
        if (chantier.getDateFinPrevue().isBefore(chantier.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin ne peut pas précéder la date de début.");
        }
        if (isBlank(chantier.getStatut())) throw new IllegalArgumentException("Le statut est obligatoire.");
        if (chantier.getAvancement() < 0 || chantier.getAvancement() > 100) {
            throw new IllegalArgumentException("L'avancement doit être compris entre 0 et 100.");
        }
        if (chantier.getIdChefDepartement() <= 0 || chantier.getIdChefProjet() <= 0) {
            throw new IllegalArgumentException("Les identifiants du chef de département et du chef de projet sont obligatoires.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
