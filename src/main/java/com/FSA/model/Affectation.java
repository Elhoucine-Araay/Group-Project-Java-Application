package com.FSA.model;

import java.time.LocalDate;

public class Affectation {

    private int idAffectation;
    private LocalDate dateAffectation;
    private LocalDate dateFin;
    private String typeRessource;
    private String statut;

    private int idChantier;
    private Integer idTache;
    private Integer idOuvrier;
    private Integer idMachine;
    private Integer idMateriel;

    public Affectation() {}

    public Affectation(int idAffectation, LocalDate dateAffectation, LocalDate dateFin, String typeRessource, String statut, int idChantier, Integer idTache, Integer idOuvrier, Integer idMachine, Integer idMateriel) {
        setIdAffectation(idAffectation);
        setDateAffectation(dateAffectation);
        setDateFin(dateFin);
        setTypeRessource(typeRessource);
        setStatut(statut);
        setIdChantier(idChantier);
        setIdTache(idTache);
        setIdOuvrier(idOuvrier);
        setIdMachine(idMachine);
        setIdMateriel(idMateriel);
    }

    public int getIdAffectation() { return idAffectation; }
    public LocalDate getDateAffectation() { return dateAffectation; }
    public LocalDate getDateFin() { return dateFin; }
    public String getTypeRessource() { return typeRessource; }
    public String getStatut() { return statut; }
    public int getIdChantier() { return idChantier; }
    public Integer getIdTache() { return idTache; }
    public Integer getIdOuvrier() { return idOuvrier; }
    public Integer getIdMachine() { return idMachine; }
    public Integer getIdMateriel() { return idMateriel; }

    public void setIdAffectation(int idAffectation) { this.idAffectation = idAffectation; }
    public void setDateAffectation(LocalDate dateAffectation) { this.dateAffectation = dateAffectation; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    public void setTypeRessource(String typeRessource) { this.typeRessource = typeRessource; }
    public void setStatut(String statut) { this.statut = statut; }
    public void setIdChantier(int idChantier) { this.idChantier = idChantier; }
    public void setIdTache(Integer idTache) { this.idTache = idTache; }
    public void setIdOuvrier(Integer idOuvrier) { this.idOuvrier = idOuvrier; }
    public void setIdMachine(Integer idMachine) { this.idMachine = idMachine; }
    public void setIdMateriel(Integer idMateriel) { this.idMateriel = idMateriel; }

    public void affecterRessource() { statut = "ACTIVE"; }

    public void modifierAffectation(LocalDate dateAffectation, LocalDate dateFin) {
        this.dateAffectation = dateAffectation; this.dateFin = dateFin;
    }

    public void annulerAffectation() { statut = "ANNULEE"; }
}