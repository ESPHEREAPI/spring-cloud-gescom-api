/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.mproduits.enums;

/**
 *
 * @author USER01
 */
public enum CategorieNotification {
    
    /**
     * Notification de création de facture
     */
    FACTURE_CREEE("Nouvelle facture", "Votre facture {numero} a été créée"),
    
    /**
     * Rappel de paiement
     */
    RAPPEL_PAIEMENT("Rappel de paiement", "Rappel : Facture {numero} à payer"),
    
    /**
     * Facture en retard
     */
    FACTURE_RETARD("Facture en retard", "Votre facture {numero} est en retard"),
    
    /**
     * Confirmation de paiement
     */
    PAIEMENT_RECU("Paiement reçu", "Paiement de {montant} reçu pour facture {numero}"),
    
    /**
     * Facture soldée
     */
    FACTURE_SOLDEE("Facture soldée", "Votre facture {numero} est soldée"),
    
    /**
     * Facture annulée
     */
    FACTURE_ANNULEE("Facture annulée", "Votre facture {numero} a été annulée"),
    
    /**
     * Relance avant échéance
     */
    RELANCE_AVANT_ECHEANCE("Échéance proche", "Facture {numero} arrive à échéance le {date}");
    
    private final String titre;
    private final String template;
    
    CategorieNotification(String titre, String template) {
        this.titre = titre;
        this.template = template;
    }
    
    public String getTitre() {
        return titre;
    }
    
    public String getTemplate() {
        return template;
    }
}
