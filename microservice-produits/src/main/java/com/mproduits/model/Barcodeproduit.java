/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Entity
@Data
@Table(name = "barcodeproduit")
@NamedQueries({
    @NamedQuery(name = "Barcodeproduit.findAll", query = "SELECT b FROM Barcodeproduit b"),
    @NamedQuery(name = "Barcodeproduit.findById", query = "SELECT b FROM Barcodeproduit b WHERE b.id = :id"),
    @NamedQuery(name = "Barcodeproduit.findByCodeBard", query = "SELECT b FROM Barcodeproduit b WHERE b.codeBard = :codeBard")})
public class Barcodeproduit implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "codeBard")
    private String codeBard;
    @JoinColumn(name = "Prixarticlesid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private PrixArticles prixArticles;
     @Transient
     Produit produit;

    public Barcodeproduit() {
    }

    public Barcodeproduit(Long id) {
        this.id = id;
    }

    
    
}
