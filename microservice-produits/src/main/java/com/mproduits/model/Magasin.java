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
@Table(name = "magasin")
@NamedQueries({
    @NamedQuery(name = "Magasin.findAll", query = "SELECT m FROM Magasin m"),
    @NamedQuery(name = "Magasin.findById", query = "SELECT m FROM Magasin m WHERE m.id = :id"),
    @NamedQuery(name = "Magasin.findByCode", query = "SELECT m FROM Magasin m WHERE m.code = :code"),
    @NamedQuery(name = "Magasin.findByLibelle", query = "SELECT m FROM Magasin m WHERE m.libelle = :libelle")})
public class Magasin implements Serializable {

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
    @JoinColumn(name = "BoutiqueId", referencedColumnName = "id")
    @ManyToOne
    private Boutique boutique;
    @JoinColumn(name = "Villeid", referencedColumnName = "id")
    @ManyToOne
    private Ville ville;
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "magasin")
    private Collection<MagasinFournisseur> magasinFournisseurCollection;
    @JsonIgnore
    @OneToMany(mappedBy = "depotId")
    private Collection<Inventaire> inventaireCollection;
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "depotId")
    private Collection<PointVente> pointventeCollection;

    public Magasin() {
    }

    public Magasin(Long id) {
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

    public Boutique getBoutiqueId() {
        return boutique;
    }

    public void setBoutiqueId(Boutique boutiqueId) {
        this.boutique = boutiqueId;
    }

    public Ville getVilleid() {
        return ville;
    }

    public void setVilleid(Ville villeid) {
        this.ville = villeid;
    }

    public Collection<MagasinFournisseur> getMagasinFournisseurCollection() {
        return magasinFournisseurCollection;
    }

    public void setMagasinFournisseurCollection(Collection<MagasinFournisseur> magasinFournisseurCollection) {
        this.magasinFournisseurCollection = magasinFournisseurCollection;
    }

    public Collection<Inventaire> getInventaireCollection() {
        return inventaireCollection;
    }

    public void setInventaireCollection(Collection<Inventaire> inventaireCollection) {
        this.inventaireCollection = inventaireCollection;
    }

    public Collection<PointVente> getPointventeCollection() {
        return pointventeCollection;
    }

    public void setPointventeCollection(Collection<PointVente> pointventeCollection) {
        this.pointventeCollection = pointventeCollection;
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
        if (!(object instanceof Magasin)) {
            return false;
        }
        Magasin other = (Magasin) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Magasin[ id=" + id + " ]";
    }

}
