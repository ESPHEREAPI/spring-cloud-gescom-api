/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

/**
 *
 * @author USER01
 */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * Classe Notification pour stocker les notifications
 * 
 * Attributs:
 * - id: UUID unique
 * - titre: Titre court
 * - message: Message détaillé
 * - type: info | success | warning | error
 * - dateCreation: Quand créée
 * - lue: Si l'utilisateur l'a lue
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    /** ID unique */
    private String id;
    
    /** Titre notification */
    private String titre;
    
    /** Message détaillé */
    private String message;
    
    /** Type: info, success, warning, error */
    private String type;
    
    /** Date création */
    private Date dateCreation;
    
    /** Si lue par utilisateur */
    private boolean lue;
    
}
