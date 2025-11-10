/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.mproduits.enums;

/**
 *
 * @author USER01
 */
public enum StatutNotification {
    
    /**
     * Notification en attente d'envoi
     */
    EN_ATTENTE("En attente"),
    
    /**
     * Notification en cours d'envoi
     */
    EN_COURS("En cours d'envoi"),
    
    /**
     * Notification envoyée avec succès
     */
    ENVOYE("Envoyée"),
    
    /**
     * Échec d'envoi de la notification
     */
    ECHEC("Échec"),
    
    /**
     * Notification annulée
     */
    ANNULEE("Annulée");
    
    private final String libelle;
    
    StatutNotification(String libelle) {
        this.libelle = libelle;
    }
    
    public String getLibelle() {
        return libelle;
    }
    
    /**
     * Vérifie si la notification peut être renvoyée
     */
    public boolean isPeutEtreRenvoye() {
        return this == ECHEC || this == ANNULEE;
    }
}
