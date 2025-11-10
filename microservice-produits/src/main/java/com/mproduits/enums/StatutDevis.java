/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.enums;

/**
 *
 * @author USER01
 */
 // ========== ÉNUMÉRATION STATUT ==========
    
    public enum StatutDevis {
        EN_ATTENTE("En attente de validation"),
        ACCEPTE("Accepté par le client"),
        REFUSE("Refusé par le client"),
        CONVERTI("Converti en facture"),
        EXPIRE("Expiré"),
        ANNULE("Annulé");

        private final String libelle;

        StatutDevis(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }
