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
 * Énumération des modes de paiement acceptés
 * 
 * @author Analyste Développeur JAVA/JAVAEE
 * @version 1.0
 */
public enum ModePaiement {
    
    /**
     * Paiement en espèces
     */
    ESPECES("Espèces", "ESP"),
    
    /**
     * Paiement par chèque bancaire
     */
    CHEQUE("Chèque", "CHQ"),
    
    /**
     * Virement bancaire
     */
    VIREMENT("Virement bancaire", "VIR"),
    
    /**
     * Paiement par carte bancaire
     */
    CARTE_BANCAIRE("Carte bancaire", "CB"),
    
    /**
     * Mobile Money (Orange Money, MTN Mobile Money, etc.)
     */
    MOBILE_MONEY("Mobile Money", "MM"),
     ORANGE_MONEY("Orange Money", "OM"),
    
    /**
     * Paiement à crédit
     */
    CREDIT("À crédit", "CRD"),
    
    /**
     * Autres modes de paiement
     */
    AUTRE("Autre", "AUT");
    
    private final String libelle;
    private final String code;
    
    ModePaiement(String libelle, String code) {
        this.libelle = libelle;
        this.code = code;
    }
    
    public String getLibelle() {
        return libelle;
    }
    
    public String getCode() {
        return code;
    }
    
    /**
     * Vérifie si le paiement nécessite une référence obligatoire
     */
    public boolean isReferenceObligatoire() {
        return this == CHEQUE || this == VIREMENT || this == MOBILE_MONEY;
    }
    
    /**
     * Vérifie si le paiement est immédiat
     */
    public boolean isPaiementImmediat() {
        return this != CREDIT;
    }
}
