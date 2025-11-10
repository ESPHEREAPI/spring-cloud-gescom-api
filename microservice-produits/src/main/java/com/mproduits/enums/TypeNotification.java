/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.enums;

/**
 *
 * @author USER01
 */
public enum TypeNotification {
      /**
     * Notification par email
     */
    EMAIL("Email", "email"),
    
    /**
     * Notification par SMS
     */
    SMS("SMS", "sms"),
    
    /**
     * Notification WhatsApp
     */
    WHATSAPP("WhatsApp", "whatsapp"),
    
    /**
     * Notification système (dans l'application)
     */
    SYSTEME("Système", "system");
    
    private final String libelle;
    private final String canal;
    
    TypeNotification(String libelle, String canal) {
        this.libelle = libelle;
        this.canal = canal;
    }
    
    public String getLibelle() {
        return libelle;
    }
    
    public String getCanal() {
        return canal;
    }
}
