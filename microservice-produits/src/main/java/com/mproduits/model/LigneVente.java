/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Entity
@Data
@Table(name = "lignevente")
public class LigneVente implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
     @ManyToOne(optional = false)
      @JsonBackReference
    private Vente vente;

    @ManyToOne(optional = false)
    private Produit produit;

    private BigDecimal quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal remise;
    private BigDecimal totalLigne;

   
    
}
