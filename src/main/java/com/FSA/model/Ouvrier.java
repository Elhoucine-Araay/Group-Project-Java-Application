package com.FSA.model;

public class Ouvrier {

    private int idOuvrier;
    private String nom;
    private String prenom;
    private String specialite;
    private String telephone;
    private double salaireJournalier;

    public Ouvrier() {}

    public Ouvrier(int idOuvrier, String nom, String prenom, String specialite, String telephone, double salaireJournalier) {
        setIdOuvrier(idOuvrier);
        setNom(nom);
        setPrenom(prenom);
        setSpecialite(specialite);
        setTelephone(telephone);
        setSalaireJournalier(salaireJournalier);
    }

    public int getIdOuvrier() { return idOuvrier; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getSpecialite() { return specialite; }
    public String getTelephone() { return telephone; }
    public double getSalaireJournalier() { return salaireJournalier; }

    public void setIdOuvrier(int idOuvrier) { this.idOuvrier = idOuvrier; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setSalaireJournalier(double salaireJournalier) { this.salaireJournalier = salaireJournalier; }

    public void modifierInformations(String nom, String prenom, String specialite, String telephone, double salaireJournalier) {
        this.nom = nom; this.prenom = prenom; this.specialite = specialite;
        this.telephone = telephone; this.salaireJournalier = salaireJournalier;
    }

    public String consulterInformations() {
        return prenom + " " + nom + " - " + specialite + " - " + telephone;
    }
}

