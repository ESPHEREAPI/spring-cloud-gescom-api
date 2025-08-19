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
@Table(name = "pertearticles")
@NamedQueries({
    @NamedQuery(name = "Pertearticles.findAll", query = "SELECT p FROM Pertearticles p"),
    @NamedQuery(name = "Pertearticles.findById", query = "SELECT p FROM Pertearticles p WHERE p.id = :id"),
    @NamedQuery(name = "Pertearticles.findByDateEnregistrement", query = "SELECT p FROM Pertearticles p WHERE p.dateEnregistrement = :dateEnregistrement"),
    @NamedQuery(name = "Pertearticles.findByPrixVente", query = "SELECT p FROM Pertearticles p WHERE p.prixVente = :prixVente"),
    @NamedQuery(name = "Pertearticles.findByQuantiteProduit", query = "SELECT p FROM Pertearticles p WHERE p.quantiteProduit = :quantiteProduit")})
public class Pertearticles implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "DateEnregistrement")
    @Temporal(TemporalType.DATE)
    private Date dateEnregistrement;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "PrixVente")
    private BigDecimal prixVente;
    @Column(name = "quantiteProduit")
    private BigDecimal quantiteProduit;
    @JoinColumn(name = "Moisid", referencedColumnName = "id")
    @ManyToOne
    private Mois mois;
    @JoinColumn(name = "Prixarticlesid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private PrixArticles prixArticlesId;

    public Pertearticles() {
    }

    public Pertearticles(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDateEnregistrement() {
        return dateEnregistrement;
    }

    public void setDateEnregistrement(Date dateEnregistrement) {
        this.dateEnregistrement = dateEnregistrement;
    }

    public BigDecimal getPrixVente() {
        return prixVente;
    }

    public void setPrixVente(BigDecimal prixVente) {
        this.prixVente = prixVente;
    }

    public BigDecimal getQuantiteProduit() {
        return quantiteProduit;
    }

    public void setQuantiteProduit(BigDecimal quantiteProduit) {
        this.quantiteProduit = quantiteProduit;
    }

    public Mois getMois() {
        return mois;
    }

    public void setMois(Mois moisid) {
        this.mois = moisid;
    }

    public PrixArticles getPrixArticlesId() {
        return prixArticlesId;
    }

    public void setPrixArticlesId(PrixArticles prixArticlesId) {
        this.prixArticlesId = prixArticlesId;
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
        if (!(object instanceof Pertearticles)) {
            return false;
        }
        Pertearticles other = (Pertearticles) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Pertearticles[ id=" + id + " ]";
    }
    
}
