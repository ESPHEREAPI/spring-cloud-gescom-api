/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "specifique")
@NamedQueries({
    @NamedQuery(name = "Specifique.findAll", query = "SELECT s FROM Specifique s"),
    @NamedQuery(name = "Specifique.findById", query = "SELECT s FROM Specifique s WHERE s.id = :id"),
    @NamedQuery(name = "Specifique.findByCode", query = "SELECT s FROM Specifique s WHERE s.code = :code"),
    @NamedQuery(name = "Specifique.findByLibelle", query = "SELECT s FROM Specifique s WHERE s.libelle = :libelle")})
public class Specifique implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "Code")
    private String code;
    @Column(name = "Libelle")
    private String libelle;
    @JsonIgnore
    @OneToMany(mappedBy = "specifiqueid")
    private Collection<Produit> produitCollection;
     @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "specifiqueId",fetch = FetchType.LAZY)
    private Collection<Specificationarticles> specificationarticlesCollection;

    public Specifique() {
    }

    public Specifique(Long id) {
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

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Collection<Produit> getProduitCollection() {
        return produitCollection;
    }

    public void setProduitCollection(Collection<Produit> produitCollection) {
        this.produitCollection = produitCollection;
    }

    public Collection<Specificationarticles> getSpecificationarticlesCollection() {
        return specificationarticlesCollection;
    }

    public void setSpecificationarticlesCollection(Collection<Specificationarticles> specificationarticlesCollection) {
        this.specificationarticlesCollection = specificationarticlesCollection;
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
        if (!(object instanceof Specifique)) {
            return false;
        }
        Specifique other = (Specifique) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Specifique[ id=" + id + " ]";
    }
    
}
