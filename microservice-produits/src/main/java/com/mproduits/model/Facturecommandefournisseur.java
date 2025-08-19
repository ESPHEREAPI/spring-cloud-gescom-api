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
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "facturecommandefournisseur")
@NamedQueries({
    @NamedQuery(name = "Facturecommandefournisseur.findAll", query = "SELECT f FROM Facturecommandefournisseur f"),
    @NamedQuery(name = "Facturecommandefournisseur.findById", query = "SELECT f FROM Facturecommandefournisseur f WHERE f.id = :id"),
    @NamedQuery(name = "Facturecommandefournisseur.findByPrixUnitaireTTc", query = "SELECT f FROM Facturecommandefournisseur f WHERE f.prixUnitaireTTc = :prixUnitaireTTc"),
    @NamedQuery(name = "Facturecommandefournisseur.findByQuantiteProduitRecuCommande", query = "SELECT f FROM Facturecommandefournisseur f WHERE f.quantiteProduitRecuCommande = :quantiteProduitRecuCommande"),
    @NamedQuery(name = "Facturecommandefournisseur.findByQuantiteProduitRecuFacture", query = "SELECT f FROM Facturecommandefournisseur f WHERE f.quantiteProduitRecuFacture = :quantiteProduitRecuFacture")})
public class Facturecommandefournisseur implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "PrixUnitaireTTc")
    private BigDecimal prixUnitaireTTc;
    @Column(name = "quantiteProduitRecuCommande")
    private BigDecimal quantiteProduitRecuCommande;
    @Column(name = "quantiteProduitRecuFacture")
    private BigDecimal quantiteProduitRecuFacture;
  
    @JoinColumn(name = "Produit", referencedColumnName = "Libelle")
    @ManyToOne(optional = false)
    private Produit produit;

    public Facturecommandefournisseur() {
    }

    public Facturecommandefournisseur(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getPrixUnitaireTTc() {
        return prixUnitaireTTc;
    }

    public void setPrixUnitaireTTc(BigDecimal prixUnitaireTTc) {
        this.prixUnitaireTTc = prixUnitaireTTc;
    }

    public BigDecimal getQuantiteProduitRecuCommande() {
        return quantiteProduitRecuCommande;
    }

    public void setQuantiteProduitRecuCommande(BigDecimal quantiteProduitRecuCommande) {
        this.quantiteProduitRecuCommande = quantiteProduitRecuCommande;
    }

    public BigDecimal getQuantiteProduitRecuFacture() {
        return quantiteProduitRecuFacture;
    }

    public void setQuantiteProduitRecuFacture(BigDecimal quantiteProduitRecuFacture) {
        this.quantiteProduitRecuFacture = quantiteProduitRecuFacture;
    }

 
    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Facturecommandefournisseur)) {
            return false;
        }
        Facturecommandefournisseur other = (Facturecommandefournisseur) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Facturecommandefournisseur[ id=" + id + " ]";
    }
    
}
