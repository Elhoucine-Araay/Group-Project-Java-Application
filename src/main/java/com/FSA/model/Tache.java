package com.FSA.model;

import java.time.LocalDate;

public class Tache {

    private int idTache;
    private String nom;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFinPrevue;
    private String statut;
    private double pourcentageAvancement;

    private int idChantier;

    public Tache() {}

    public Tache(int idTache, String nom, String description, LocalDate dateDebut, LocalDate dateFinPrevue, String statut, double pourcentageAvancement, int idChantier) {
        setIdTache(idTache);
        setNom(nom);
        setDescription(description);
        setDateDebut(dateDebut);
        setDateFinPrevue(dateFinPrevue);
        setStatut(statut);
        setPourcentageAvancement(pourcentageAvancement);
        setIdChantier(idChantier);
    }

    public int getIdTache() { return idTache; }
    public String getNom() { return nom; }
    public String getDescription() { return description; }
    public LocalDate getDateDebut() { return dateDebut; }
    public LocalDate getDateFinPrevue() { return dateFinPrevue; }
    public String getStatut() { return statut; }
    public double getPourcentageAvancement() { return pourcentageAvancement; }
    public int getIdChantier() { return idChantier; }

    public void setIdTache(int idTache) { this.idTache = idTache; }
    public void setNom(String nom) { this.nom = nom; }
    public void setDescription(String description) { this.description = description; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public void setDateFinPrevue(LocalDate dateFinPrevue) { this.dateFinPrevue = dateFinPrevue; }
    public void setStatut(String statut) { this.statut = statut; }
    public void setPourcentageAvancement(double pourcentageAvancement) {
        if (pourcentageAvancement < 0 || pourcentageAvancement > 100)
            throw new IllegalArgumentException("L'avancement doit être compris entre 0 et 100.");
        this.pourcentageAvancement = pourcentageAvancement;
    }
    public void setIdChantier(int idChantier) { this.idChantier = idChantier; }

    public void changerStatut(String nouveauStatut) { this.statut = nouveauStatut; }

    /** Met à jour l'avancement (0 à 100). À 100 %, la tâche passe au statut terminee. */
    public void mettreAJourAvancement(double pourcentage) {
        setPourcentageAvancement(pourcentage);
        if (pourcentage == 100) statut = "TERMINEE";
    }
}
