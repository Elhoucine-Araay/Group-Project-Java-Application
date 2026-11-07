package com.FSA.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Chantier {

    private int idChantier;
    private String nom;
    private String adresse;
    private LocalDate dateDebut;
    private LocalDate dateFinPrevue;
    private String statut;
    private double avancement;
    private int idChefDepartement;
    private int idChefProjet;

    public Chantier() {}

    public Chantier(int idChantier, String nom, String adresse, LocalDate dateDebut, LocalDate dateFinPrevue, String statut, double avancement, int idChefDepartement, int idChefProjet) {
        setIdChantier(idChantier);
        setNom(nom);
        setAdresse(adresse);
        setDateDebut(dateDebut);
        setDateFinPrevue(dateFinPrevue);
        setStatut(statut);
        setAvancement(avancement);
        setIdChefDepartement(idChefDepartement);
        setIdChefProjet(idChefProjet);
    }

    public int getIdChantier() { return idChantier; }
    public String getNom() { return nom; }
    public String getAdresse() { return adresse; }
    public LocalDate getDateDebut() { return dateDebut; }
    public LocalDate getDateFinPrevue() { return dateFinPrevue; }
    public String getStatut() { return statut; }
    public double getAvancement() { return avancement; }
    public int getIdChefDepartement() { return idChefDepartement; }
    public int getIdChefProjet() { return idChefProjet; }

    public void setIdChantier(int idChantier) { this.idChantier = idChantier; }
    public void setNom(String nom) { this.nom = nom; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public void setDateFinPrevue(LocalDate dateFinPrevue) { this.dateFinPrevue = dateFinPrevue; }
    public void setStatut(String statut) { this.statut = statut; }
    public void setAvancement(double avancement) { this.avancement = avancement; }
    public void setIdChefDepartement(int idChefDepartement) { this.idChefDepartement = idChefDepartement; }
    public void setIdChefProjet(int idChefProjet) { this.idChefProjet = idChefProjet; }

    private final List<Tache> taches = new ArrayList<>();

    public List<Tache> getTaches() { return taches; }

    public void ajouterTache(Tache tache) {
        tache.setIdChantier(idChantier);
        taches.add(tache);
        calculerAvancement();
    }

    public void modifierChantier(String nom, String adresse, LocalDate dateDebut, LocalDate dateFinPrevue, String statut) {
        this.nom = nom; this.adresse = adresse; this.dateDebut = dateDebut;
        this.dateFinPrevue = dateFinPrevue; this.statut = statut;
    }

    public double calculerAvancement() {
        if (taches.isEmpty()) { avancement = 0; return 0; }
        double somme = 0;
        for (Tache t : taches) somme += t.getPourcentageAvancement();
        avancement = somme / taches.size();
        return avancement;
    }

    public double consulterAvancement() { return avancement; }
}