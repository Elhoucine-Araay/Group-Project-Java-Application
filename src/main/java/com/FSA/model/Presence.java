package com.FSA.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

/** Les heures sont au format HH:mm (ex. 08:00). */
public class Presence {

    private int idPresence;
    private LocalDate date;
    private String heureArrivee;
    private String heureDepart;
    private String statut;
    private String observation;
    private int idOuvrier;

    public Presence() {}

    public Presence(int idPresence, LocalDate date, String heureArrivee, String heureDepart, String statut, String observation, int idOuvrier) {
        setIdPresence(idPresence);
        setDate(date);
        setHeureArrivee(heureArrivee);
        setHeureDepart(heureDepart);
        setStatut(statut);
        setObservation(observation);
        setIdOuvrier(idOuvrier);
    }

    public int getIdPresence() { return idPresence; }
    public LocalDate getDate() { return date; }
    public String getHeureArrivee() { return heureArrivee; }
    public String getHeureDepart() { return heureDepart; }
    public String getStatut() { return statut; }
    public String getObservation() { return observation; }
    public int getIdOuvrier() { return idOuvrier; }

    public void setIdPresence(int idPresence) { this.idPresence = idPresence; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setHeureArrivee(String heureArrivee) { this.heureArrivee = heureArrivee; }
    public void setHeureDepart(String heureDepart) { this.heureDepart = heureDepart; }
    public void setStatut(String statut) { this.statut = statut; }
    public void setObservation(String observation) { this.observation = observation; }
    public void setIdOuvrier(int idOuvrier) { this.idOuvrier = idOuvrier; }

    public boolean verifierPresence() { return "PRESENT".equals(statut) || "RETARD".equals(statut); }

    public double calculerHeuresTravaillees() {
        if (heureArrivee == null || heureDepart == null) return 0;
        return Duration.between(LocalTime.parse(heureArrivee), LocalTime.parse(heureDepart)).toMinutes() / 60.0;
    }
}

