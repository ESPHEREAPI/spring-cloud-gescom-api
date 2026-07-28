/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "prixachat")

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PrixAchat implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @JoinColumn(name = "produitid", referencedColumnName = "id")
    @ManyToOne
    private Produit produit;
    @Column(name = "datedebut", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date datedebut;
    @Column(name = "datefin", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date datefin;
    BigDecimal prix=BigDecimal.ZERO;
     private String usercreat;
   
    
}
