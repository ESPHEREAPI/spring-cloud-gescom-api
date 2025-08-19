/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "prixclient")
@NamedQueries({
    @NamedQuery(name = "Prixclient.findAll", query = "SELECT p FROM Prixclient p"),
    @NamedQuery(name = "Prixclient.findById", query = "SELECT p FROM Prixclient p WHERE p.id = :id"),
    @NamedQuery(name = "Prixclient.findByActif", query = "SELECT p FROM Prixclient p WHERE p.actif = :actif"),
    @NamedQuery(name = "Prixclient.findByMontant", query = "SELECT p FROM Prixclient p WHERE p.montant = :montant")})
public class Prixclient implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @Column(name = "actif")
    private boolean actif;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "montant")
    private BigDecimal montant;
    @JoinColumn(name = "Clientid", referencedColumnName = "id")
    @ManyToOne
    private Client clientid;
    @JoinColumns({
        @JoinColumn(name = "AnneeId", referencedColumnName = "AnneeId"),
        @JoinColumn(name = "EmployeurId", referencedColumnName = "EmployeurId")})
    @ManyToOne(optional = false)
    private Entreprise entreprise;
    @JoinColumn(name = "Produitid", referencedColumnName = "id")
    @ManyToOne
    private Produit produitid;
  //  @OneToMany(cascade = CascadeType.ALL, mappedBy = "prixClientid")
    //private Collection<Livraison_old> livraisonCollection;

    public Prixclient() {
    }

    public Prixclient(Long id) {
        this.id = id;
    }

    public Prixclient(Long id, boolean actif) {
        this.id = id;
        this.actif = actif;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean getActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public Client getClientid() {
        return clientid;
    }

    public void setClientid(Client clientid) {
        this.clientid = clientid;
    }

    public Entreprise getEntreprise() {
        return entreprise;
    }

    public void setEntreprise(Entreprise entreprise) {
        this.entreprise = entreprise;
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
        if (!(object instanceof Prixclient)) {
            return false;
        }
        Prixclient other = (Prixclient) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Prixclient[ id=" + id + " ]";
    }
    
}
