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
@Table(name = "produitachat")
@NamedQueries({
    @NamedQuery(name = "Produitachat.findAll", query = "SELECT p FROM Produitachat p"),
    @NamedQuery(name = "Produitachat.findById", query = "SELECT p FROM Produitachat p WHERE p.id = :id"),
    @NamedQuery(name = "Produitachat.findByAchat", query = "SELECT p FROM Produitachat p WHERE p.achat = :achat"),
    @NamedQuery(name = "Produitachat.findByDateReception", query = "SELECT p FROM Produitachat p WHERE p.dateReception = :dateReception")})
public class Produitachat implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "achat")
    private BigDecimal achat;
    @Column(name = "date_reception")
    @Temporal(TemporalType.DATE)
    private Date dateReception;
    @JoinColumn(name = "Produitid", referencedColumnName = "id")
    @ManyToOne
    private Produit produitid;

    public Produitachat() {
    }

    public Produitachat(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAchat() {
        return achat;
    }

    public void setAchat(BigDecimal achat) {
        this.achat = achat;
    }

    public Date getDateReception() {
        return dateReception;
    }

    public void setDateReception(Date dateReception) {
        this.dateReception = dateReception;
    }

    public Produit getProduitid() {
        return produitid;
    }

    public void setProduitid(Produit produitid) {
        this.produitid = produitid;
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
        if (!(object instanceof Produitachat)) {
            return false;
        }
        Produitachat other = (Produitachat) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Produitachat[ id=" + id + " ]";
    }
    
}
