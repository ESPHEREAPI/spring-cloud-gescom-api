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

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "specificationarticles")
@NamedQueries({
    @NamedQuery(name = "Specificationarticles.findAll", query = "SELECT s FROM Specificationarticles s"),
    @NamedQuery(name = "Specificationarticles.findById", query = "SELECT s FROM Specificationarticles s WHERE s.id = :id"),
    @NamedQuery(name = "Specificationarticles.findByLibelle", query = "SELECT s FROM Specificationarticles s WHERE s.libelle = :libelle")})
public class Specificationarticles implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "libelle")
    private String libelle;
    @JoinColumn(name = "artticleid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Produit artticleId;
    @JoinColumn(name = "specifiqueid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Specifique specifiqueId;

    public Specificationarticles() {
    }

    public Specificationarticles(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Produit getArtticleId() {
        return artticleId;
    }

    public void setArtticleId(Produit artticleId) {
        this.artticleId = artticleId;
    }

    public Specifique getSpecifiqueId() {
        return specifiqueId;
    }

    public void setSpecifiqueId(Specifique specifiqueId) {
        this.specifiqueId = specifiqueId;
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
        if (!(object instanceof Specificationarticles)) {
            return false;
        }
        Specificationarticles other = (Specificationarticles) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Specificationarticles[ id=" + id + " ]";
    }
    
}
