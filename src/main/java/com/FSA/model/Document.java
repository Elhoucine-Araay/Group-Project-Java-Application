package com.FSA.model;

import java.time.LocalDate;

public class Document {

    private int idDocument;
    private String nom;
    private String type;
    private LocalDate dateCreation;
    private String cheminFichier;
    private String description;

    // Relations du diagramme (clés étrangères)
    private int idChantier;

    public Document() {}

    public Document(int idDocument, String nom, String type, LocalDate dateCreation, String cheminFichier, String description, int idChantier) {
        setIdDocument(idDocument);
        setNom(nom);
        setType(type);
        setDateCreation(dateCreation);
        setCheminFichier(cheminFichier);
        setDescription(description);
        setIdChantier(idChantier);
    }

    public int getIdDocument() { return idDocument; }
    public String getNom() { return nom; }
    public String getType() { return type; }
    public LocalDate getDateCreation() { return dateCreation; }
    public String getCheminFichier() { return cheminFichier; }
    public String getDescription() { return description; }
    public int getIdChantier() { return idChantier; }

    public void setIdDocument(int idDocument) { this.idDocument = idDocument; }
    public void setNom(String nom) { this.nom = nom; }
    public void setType(String type) { this.type = type; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }
    public void setCheminFichier(String cheminFichier) { this.cheminFichier = cheminFichier; }
    public void setDescription(String description) { this.description = description; }
    public void setIdChantier(int idChantier) { this.idChantier = idChantier; }
}