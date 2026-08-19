package com.mproduits.enums;

/**
 * Canal d'origine d'un Devis - permet a l'ecran admin "Commandes en ligne"
 * de distinguer les devis crees par le personnel (INTERNE) de ceux soumis
 * par un client via le site public e-commerce (EN_LIGNE), sans avoir besoin
 * d'une entite de commande separee (voir StockRestaurationService/Devis
 * pour le meme genre de reutilisation de modele existant).
 */
public enum CanalOrigine {
    INTERNE("Cree en interne"),
    EN_LIGNE("Commande en ligne");

    private final String libelle;

    CanalOrigine(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
