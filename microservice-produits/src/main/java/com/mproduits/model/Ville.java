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
@Table(name = "ville")
@NamedQueries({
    @NamedQuery(name = "Ville.findAll", query = "SELECT v FROM Ville v"),
    @NamedQuery(name = "Ville.findById", query = "SELECT v FROM Ville v WHERE v.id = :id"),
    @NamedQuery(name = "Ville.findByCode", query = "SELECT v FROM Ville v WHERE v.code = :code"),
    @NamedQuery(name = "Ville.findByLibelle", query = "SELECT v FROM Ville v WHERE v.libelle = :libelle")})
public class Ville implements Serializable {

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
    @OneToMany(mappedBy = "ville")
    private Collection<Fournisseur> fournisseurCollection;
    @JsonIgnore
    @OneToMany(mappedBy = "ville")
    private Collection<Magasin> magasinCollection;
    @JsonIgnore
    @OneToMany(mappedBy = "ville")
    private Collection<Client> clientCollection;
    @JsonIgnore
    @OneToMany(mappedBy = "ville")
    private Collection<Boutique> boutiqueCollection;
    @JsonIgnore
    @OneToMany(mappedBy = "ville")
    private Collection<Serviceclient> serviceclientCollection;

    public Ville() {
    }

    public Ville(Long id) {
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

    public Collection<Fournisseur> getFournisseurCollection() {
        return fournisseurCollection;
    }

    public void setFournisseurCollection(Collection<Fournisseur> fournisseurCollection) {
        this.fournisseurCollection = fournisseurCollection;
    }

    public Collection<Magasin> getMagasinCollection() {
        return magasinCollection;
    }

    public void setMagasinCollection(Collection<Magasin> magasinCollection) {
        this.magasinCollection = magasinCollection;
    }

    public Collection<Client> getClientCollection() {
        return clientCollection;
    }

    public void setClientCollection(Collection<Client> clientCollection) {
        this.clientCollection = clientCollection;
    }

    public Collection<Boutique> getBoutiqueCollection() {
        return boutiqueCollection;
    }

    public void setBoutiqueCollection(Collection<Boutique> boutiqueCollection) {
        this.boutiqueCollection = boutiqueCollection;
    }

    public Collection<Serviceclient> getServiceclientCollection() {
        return serviceclientCollection;
    }

    public void setServiceclientCollection(Collection<Serviceclient> serviceclientCollection) {
        this.serviceclientCollection = serviceclientCollection;
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
        if (!(object instanceof Ville)) {
            return false;
        }
        Ville other = (Ville) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Ville[ id=" + id + " ]";
    }

}
