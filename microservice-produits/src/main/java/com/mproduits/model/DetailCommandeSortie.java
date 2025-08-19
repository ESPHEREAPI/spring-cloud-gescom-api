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
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "detailcommandesortie")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailCommandeSortie implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private BigDecimal quantitsortie = BigDecimal.ZERO;
    @Column(name = "datesortie")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datesortie;
    private BigDecimal prixachat = BigDecimal.ZERO;
    private BigDecimal prixvente = BigDecimal.ZERO;
    private String usercreate;
   
    @JoinColumn(name = "commandeid", referencedColumnName = "id")
    @ManyToOne
    private Commande commande;
     @JoinColumn(name = "Moisid", referencedColumnName = "id")
    @ManyToOne
    private Mois mois;
    
}
