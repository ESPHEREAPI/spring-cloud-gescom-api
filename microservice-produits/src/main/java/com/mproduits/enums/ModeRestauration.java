package com.mproduits.enums;

/**
 * Mode d'application d'un fichier de restauration de stock (voir
 * StockRestaurationService) : AJOUT ajoute la quantite du fichier au stock
 * existant, REMPLACEMENT l'ecrase.
 */
public enum ModeRestauration {
    AJOUT, REMPLACEMENT
}
