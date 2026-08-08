/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mproduits.enums.TypeMagasin;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler","magasinFournisseurCollection", "inventaireCollection", "pointventeCollection"})
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
    // Type explicite (voir TypeMagasin) - nullable pour les lignes existantes
    // avant migration ; getTypeMagasin() retombe sur l'ancienne deduction
    // (boutique null/non-null) si la colonne n'a pas encore ete renseignee.
    @Column(name = "type_magasin")
    @Enumerated(EnumType.STRING)
    private TypeMagasin typeMagasin;
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

    // Rattachement compagnie direct (isolation multi-tenant) - necessaire car un
    // Magasin peut exister sans Boutique (depot de stock central), donc ne peut
    // pas toujours deriver sa compagnie via boutique.compagnie.
    @JsonIgnore
    @JoinColumn(name = "compagnie_id")
    @ManyToOne
    private Compagnie compagnie;

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

    public TypeMagasin getTypeMagasin() {
        if (typeMagasin != null) {
            return typeMagasin;
        }
        // Repli pour les lignes creees avant l'ajout de cette colonne.
        return boutique != null ? TypeMagasin.POINT_DE_VENTE : TypeMagasin.DEPOT_CENTRAL;
    }

    public void setTypeMagasin(TypeMagasin typeMagasin) {
        this.typeMagasin = typeMagasin;
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

    public Compagnie getCompagnie() {
        return compagnie;
    }

    public void setCompagnie(Compagnie compagnie) {
        this.compagnie = compagnie;
    }

    public Long getCompagnieId() {
        return compagnie != null ? compagnie.getId() : null;
    }

    public void setCompagnieId(Long compagnieId) {
        this.compagnie = compagnieId != null ? new Compagnie(compagnieId) : null;
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
