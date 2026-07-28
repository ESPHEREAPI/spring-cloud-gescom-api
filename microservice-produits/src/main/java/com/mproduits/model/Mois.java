/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Collection;

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "mois")
@NamedQueries({
    @NamedQuery(name = "Mois.findAll", query = "SELECT m FROM Mois m"),
    @NamedQuery(name = "Mois.findById", query = "SELECT m FROM Mois m WHERE m.id = :id"),
    @NamedQuery(name = "Mois.findByCode", query = "SELECT m FROM Mois m WHERE m.code = :code"),
    @NamedQuery(name = "Mois.findByNumero", query = "SELECT m FROM Mois m WHERE m.numero = :numero"),
    @NamedQuery(name = "Mois.findByMois", query = "SELECT m FROM Mois m WHERE m.mois = :mois")})
public class Mois implements Serializable {

    @OneToMany(mappedBy = "mois")
    private Collection<Taxeproduit> taxproduitCollection;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "Code")
    private String code;
    @Column(name = "numero")
    private Integer numero;
    @Column(name = "mois")
    private String mois;
    @JsonIgnore
    @OneToMany(mappedBy = "mois")
    private Collection<Pertearticles> pertearticlesCollection;
      @JsonIgnore
    @OneToMany(mappedBy = "mois")
    private Collection<Quinzaine> quinzaineCollection;
   
   
//    private Collection<Livraison_old> livraisonCollection;
    @JoinColumn(name = "anneeid", referencedColumnName = "Id")
    @ManyToOne
    private Annee annee;

    public Mois() {
    }

    public Mois(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getMois() {
        return mois;
    }

    public void setMois(String mois) {
        this.mois = mois;
    }

    public Collection<Pertearticles> getPertearticlesCollection() {
        return pertearticlesCollection;
    }

    public void setPertearticlesCollection(Collection<Pertearticles> pertearticlesCollection) {
        this.pertearticlesCollection = pertearticlesCollection;
    }

    public Collection<Quinzaine> getQuinzaineCollection() {
        return quinzaineCollection;
    }

    public void setQuinzaineCollection(Collection<Quinzaine> quinzaineCollection) {
        this.quinzaineCollection = quinzaineCollection;
    }

  

 

   

    public Annee getAnnee() {
        return annee;
    }

    public void setAnnee(Annee anneeid) {
        this.annee = anneeid;
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
        if (!(object instanceof Mois)) {
            return false;
        }
        Mois other = (Mois) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Mois[ id=" + id + " ]";
    }

    public Collection<Taxeproduit> getTaxproduitCollection() {
        return taxproduitCollection;
    }

    public void setTaxproduitCollection(Collection<Taxeproduit> taxproduitCollection) {
        this.taxproduitCollection = taxproduitCollection;
    }
    
}
