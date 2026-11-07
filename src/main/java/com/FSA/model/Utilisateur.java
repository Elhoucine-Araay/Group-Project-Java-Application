package com.FSA.model;

public class Utilisateur {

    private int idUtilisateur;
    private String nom;
    private String email;
    private String motDePasse;

    public Utilisateur() {}

    public Utilisateur(int idUtilisateur, String nom, String email, String motDePasse) {
        setIdUtilisateur(idUtilisateur);
        setNom(nom);
        setEmail(email);
        setMotDePasse(motDePasse);
    }

    public int getIdUtilisateur() { return idUtilisateur; }
    public String getNom() { return nom; }
    public String getEmail() { return email; }
    public String getMotDePasse() { return motDePasse; }

    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }
    public void setNom(String nom) { this.nom = nom; }
    public void setEmail(String email) { this.email = email; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
}
