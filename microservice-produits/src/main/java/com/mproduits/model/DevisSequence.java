/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "devis_sequence")
public class DevisSequence implements Serializable {

    private static final long serialVersionUID = 1L;
   
    @Id
    @Column(name = "annee")
    private Integer annee;
    
    @Column(name = "dernier_numero", nullable = false)
    private Integer dernierNumero = 0;
    
    // Constructeurs
    public DevisSequence() {}
    
    public DevisSequence(Integer annee) {
        this.annee = annee;
        this.dernierNumero = 0;
    }
    
    // Getters et Setters
    public Integer getAnnee() { return annee; }
    public void setAnnee(Integer annee) { this.annee = annee; }
    
    public Integer getDernierNumero() { return dernierNumero; }
    public void setDernierNumero(Integer dernierNumero) { this.dernierNumero = dernierNumero; }
    
}
