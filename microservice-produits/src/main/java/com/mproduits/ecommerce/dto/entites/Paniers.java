/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.ecommerce.dto.entites;

import com.mproduits.model.PrixArticles;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author USER01
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paniers implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "Date_enregistrement")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date_enregistrement;
    @Column(name = "montant")
    private BigDecimal montant;
    @Column(name = "num_panier")
    private String num_panier;
    @Column(name = "quantite_produit")
    private BigDecimal quantite;
    @Column(name = "Heure")
    @Temporal(TemporalType.TIME)
    private Date heure;
    @JoinColumn(name = "PrixArticlesId", referencedColumnName = "id")
    @ManyToOne
    private PrixArticles prixArticlesId;
    private String test;

}
