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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "inventaire")
@NamedQueries({
    @NamedQuery(name = "Inventaire.findAll", query = "SELECT i FROM Inventaire i"),
    @NamedQuery(name = "Inventaire.findById", query = "SELECT i FROM Inventaire i WHERE i.id = :id"),
    @NamedQuery(name = "Inventaire.findByDateCreation", query = "SELECT i FROM Inventaire i WHERE i.dateCreation = :dateCreation"),
    @NamedQuery(name = "Inventaire.findByHeure", query = "SELECT i FROM Inventaire i WHERE i.heure = :heure"),
    @NamedQuery(name = "Inventaire.findByPrix", query = "SELECT i FROM Inventaire i WHERE i.prix = :prix"),
    @NamedQuery(name = "Inventaire.findByQuantite", query = "SELECT i FROM Inventaire i WHERE i.quantite = :quantite")})
public class Inventaire implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "DateCreation")
    @Temporal(TemporalType.DATE)
    private Date dateCreation;
    @Column(name = "heure")
    @Temporal(TemporalType.TIME)
    private Date heure;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "prix")
    private BigDecimal prix;
    @Column(name = "quantite")
    private BigDecimal quantite;
    @JoinColumn(name = "Boutiqueid", referencedColumnName = "id")
    @ManyToOne
    private Boutique boutique;
    @JoinColumn(name = "DepotId", referencedColumnName = "id")
    @ManyToOne
    private Magasin depotId;
    @JoinColumn(name = "ProduitId", referencedColumnName = "id")
    @ManyToOne
    private Produit produitId;

    public Inventaire() {
    }

    public Inventaire(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Date getHeure() {
        return heure;
    }

    public void setHeure(Date heure) {
        this.heure = heure;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }

    public BigDecimal getQuantite() {
        return quantite;
    }

    public void setQuantite(BigDecimal quantite) {
        this.quantite = quantite;
    }

    public Boutique getBoutiqueid() {
        return boutique;
    }

    public void setBoutiqueid(Boutique boutiqueid) {
        this.boutique = boutiqueid;
    }

    public Magasin getDepotId() {
        return depotId;
    }

    public void setDepotId(Magasin depotId) {
        this.depotId = depotId;
    }

    public Produit getProduitId() {
        return produitId;
    }

    public void setProduitId(Produit produitId) {
        this.produitId = produitId;
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
        if (!(object instanceof Inventaire)) {
            return false;
        }
        Inventaire other = (Inventaire) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Inventaire[ id=" + id + " ]";
    }
    
}
