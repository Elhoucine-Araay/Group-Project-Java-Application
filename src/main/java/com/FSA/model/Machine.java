package com.FSA.model;

import java.time.LocalDate;

public class Machine {

    private int idMachine;
    private String nom;
    private String type;
    private String etat;
    private LocalDate dateDerniereMaintenance;
    private boolean disponible;

    public Machine() {}

    public Machine(int idMachine, String nom, String type, String etat, LocalDate dateDerniereMaintenance, boolean disponible) {
        setIdMachine(idMachine);
        setNom(nom);
        setType(type);
        setEtat(etat);
        setDateDerniereMaintenance(dateDerniereMaintenance);
        setDisponible(disponible);
    }

    public int getIdMachine() { return idMachine; }
    public String getNom() { return nom; }
    public String getType() { return type; }
    public String getEtat() { return etat; }
    public LocalDate getDateDerniereMaintenance() { return dateDerniereMaintenance; }
    public boolean isDisponible() { return disponible; }

    public void setIdMachine(int idMachine) { this.idMachine = idMachine; }
    public void setNom(String nom) { this.nom = nom; }
    public void setType(String type) { this.type = type; }
    public void setEtat(String etat) { this.etat = etat; }
    public void setDateDerniereMaintenance(LocalDate dateDerniereMaintenance) { this.dateDerniereMaintenance = dateDerniereMaintenance; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public String verifierEtat() { return etat; }

    public void modifierEtat(String nouvelEtat) {
        this.etat = nouvelEtat;
        this.disponible = "OPERATIONNELLE".equals(nouvelEtat);
    }

    public void signalerPanne() { modifierEtat("EN_PANNE"); }

    public void mettreAJourMaintenance(LocalDate date) {
        this.dateDerniereMaintenance = date;
        modifierEtat("OPERATIONNELLE");
    }
    /** Une machine en maintenance ou en panne ne peut pas être affectée à un chantier. */
    public boolean estAffectable() { return !"EN_MAINTENANCE".equals(etat) && !"EN_PANNE".equals(etat); }
}