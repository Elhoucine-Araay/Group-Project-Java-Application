
package com.FSA.service;

import com.FSA.DAO.UtilisateurDAO;

public class UtilisateurService {

    private final UtilisateurDAO utilisateurDAO;

    public UtilisateurService() {
        this.utilisateurDAO = new UtilisateurDAO();
    }

    public UtilisateurDAO.Connexion connecter(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) return null;
        return utilisateurDAO.connecter(email.trim(), password);
    }

    public String authentifier(String email, String password) {

        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {
            return null;
        }

        return utilisateurDAO.authentifier(email.trim(), password);
    }
}
