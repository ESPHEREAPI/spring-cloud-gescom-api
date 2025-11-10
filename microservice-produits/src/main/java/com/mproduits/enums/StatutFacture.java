/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.mproduits.enums;

/**
 *
 * @author USER01
 */
/**
 * Énumération des statuts possibles d'une facture
 * 
 * CYCLE DE VIE D'UNE FACTURE:
 * 1. BROUILLON     → Facture en cours de création, non validée
 * 2. NON_PAYEE     → Facture validée, en attente de paiement
 * 3. PARTIELLE     → Facture partiellement payée
 * 4. PAYEE         → Facture totalement payée
 * 5. EN_RETARD     → Facture non payée après échéance
 * 6. ANNULEE       → Facture annulée (remboursement/erreur)
 * 
 * @author Analyste Développeur JAVA/JAVAEE
 * @version 1.0
 */
public enum StatutFacture {
    
    /**
     * Facture en cours de création, non validée
     * Peut être modifiée librement
     */
    BROUILLON("Brouillon", "Facture en cours de création"),
    
    /**
     * Facture validée, aucun paiement effectué
     * Stock déjà sorti, ne peut plus être modifiée
     */
    NON_PAYEE("Non payée", "En attente de paiement"),
    
    /**
     * Facture partiellement payée
     * Au moins un versement effectué mais solde restant
     */
    PARTIELLEMENT_PAYEE("Partiellement payée", "Paiement partiel effectué"),
    
    /**
     * Facture totalement payée
     * Montant payé = Montant TTC
     */
    PAYEE("Payée", "Paiement complet effectué"),
    
    /**
     * Facture en retard de paiement
     * Date d'échéance dépassée et non payée
     */
    EN_RETARD("En retard", "Paiement en retard"),
    
    /**
     * Facture annulée
     * Stock réintégré, aucun paiement accepté
     */
    ANNULEE("Annulée", "Facture annulée");

    
    private final String libelle;
    private final String description;
    
    StatutFacture(String libelle, String description) {
        this.libelle = libelle;
        this.description = description;
    }
    
    public String getLibelle() {
        return libelle;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Vérifie si la facture peut être modifiée
     */
    public boolean isModifiable() {
        return this == BROUILLON;
    }
    
    /**
     * Vérifie si la facture peut recevoir des paiements
     */
    public boolean isPeutRecevoirPaiement() {
        return this == NON_PAYEE || this == PARTIELLEMENT_PAYEE || this == EN_RETARD;
    }
    
    /**
     * Vérifie si la facture peut être annulée
     */
    public boolean isPeutEtreAnnulee() {
        return this != ANNULEE && this != PAYEE;
    }
    
    /**
     * Vérifie si le stock a été sorti
     */
    public boolean isStockSorti() {
        return this != BROUILLON && this != ANNULEE;
    }
    
}
