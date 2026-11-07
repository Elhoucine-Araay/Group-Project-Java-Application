package com.FSA.model;

public class Materiel {

    private int idMateriel;
    private String nom;
    private String description;
    private double quantiteStock;
    private String unite;
    private double seuilMinimum;

    public Materiel() {}

    public Materiel(int idMateriel, String nom, String description, double quantiteStock, String unite, double seuilMinimum) {
        setIdMateriel(idMateriel);
        setNom(nom);
        setDescription(description);
        setQuantiteStock(quantiteStock);
        setUnite(unite);
        setSeuilMinimum(seuilMinimum);
    }

    public int getIdMateriel() { return idMateriel; }
    public String getNom() { return nom; }
    public String getDescription() { return description; }
    public double getQuantiteStock() { return quantiteStock; }
    public String getUnite() { return unite; }
    public double getSeuilMinimum() { return seuilMinimum; }

    public void setIdMateriel(int idMateriel) { this.idMateriel = idMateriel; }
    public void setNom(String nom) { this.nom = nom; }
    public void setDescription(String description) { this.description = description; }
    public void setQuantiteStock(double quantiteStock) {
        if (quantiteStock < 0) throw new IllegalArgumentException("La quantité en stock ne peut pas être négative.");
        this.quantiteStock = quantiteStock;
    }
    public void setUnite(String unite) { this.unite = unite; }
    public void setSeuilMinimum(double seuilMinimum) { this.seuilMinimum = seuilMinimum; }

    public void ajouterStock(double quantite) {
        if (quantite <= 0) throw new IllegalArgumentException("La quantité doit être supérieure à 0.");
        quantiteStock += quantite;
    }

    public void retirerStock(double quantite) {
        if (quantite <= 0) throw new IllegalArgumentException("La quantité doit être supérieure à 0.");
        if (quantite > quantiteStock) throw new IllegalArgumentException("Sortie supérieure au stock disponible.");
        quantiteStock -= quantite;
    }

    /** Vrai si le stock est suffisant (quantité >= seuil minimum). Faux = alerte. */
    public boolean verifierStock() { return quantiteStock >= seuilMinimum; }

    public double consulterQuantite() { return quantiteStock; }
}
