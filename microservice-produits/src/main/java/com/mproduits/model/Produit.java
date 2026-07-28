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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Entity
@Data
@Table(name = "produit")
@NamedQueries({
    @NamedQuery(name = "Produit.findAll", query = "SELECT p FROM Produit p"),
    @NamedQuery(name = "Produit.findById", query = "SELECT p FROM Produit p WHERE p.id = :id"),
    @NamedQuery(name = "Produit.findByCode", query = "SELECT p FROM Produit p WHERE p.code = :code"),
    @NamedQuery(name = "Produit.findByLibelle", query = "SELECT p FROM Produit p WHERE p.libelle = :libelle"),
    @NamedQuery(name = "Produit.findByReference", query = "SELECT p FROM Produit p WHERE p.reference = :reference"),
    @NamedQuery(name = "Produit.findByPrixVenteModifiable", query = "SELECT p FROM Produit p WHERE p.prixVenteModifiable = :prixVenteModifiable"),
    @NamedQuery(name = "Produit.findByDeletes", query = "SELECT p FROM Produit p WHERE p.deletes = :deletes"),
    @NamedQuery(name = "Produit.findByPacquets", query = "SELECT p FROM Produit p WHERE p.pacquets = :pacquets"),
    @NamedQuery(name = "Produit.findByQuantiteByPacquet", query = "SELECT p FROM Produit p WHERE p.quantiteByPacquet = :quantiteByPacquet"),
    @NamedQuery(name = "Produit.findByDateDelete", query = "SELECT p FROM Produit p WHERE p.dateDelete = :dateDelete"),
    @NamedQuery(name = "Produit.findByDeleteUser", query = "SELECT p FROM Produit p WHERE p.deleteUser = :deleteUser"),
    @NamedQuery(name = "Produit.findByPhotoName", query = "SELECT p FROM Produit p WHERE p.photoName = :photoName")})
public class Produit implements Serializable {

    @OneToMany(mappedBy = "produit")
    private Collection<Taxeproduit> taxproduitCollection;

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
    @Column(name = "Reference")
    private String reference;
    @Column(name = "prixventemodifiable")
    private Boolean prixVenteModifiable;
    @Column(name = "deletes")
    private Boolean deletes;
    @Column(name = "pacquets")
    private Boolean pacquets;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "quantitebypacquet")
    private BigDecimal quantiteByPacquet;
    @Column(name = "datedelete")
    @Temporal(TemporalType.DATE)
    private Date dateDelete;
    @Column(name = "deleteUser")
    private String deleteUser;
    @Column(name = "photo_name")
    private String photoName;
    private String description;
    private String username;
      private String usernameupdate;
     @Column(name = "prixventemodifiableaccepter")
    private BigDecimal prixVenteModifiableAccepter;
    @OneToMany(mappedBy = "produitid", fetch = FetchType.LAZY)
    @JsonIgnore
    private Collection<Produitachat> produitachatCollection;
    @OneToMany(mappedBy = "produitid", fetch = FetchType.LAZY)
    @JsonIgnore
    private Collection<Prixclient> prixclientCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "produit", fetch = FetchType.LAZY)
    @JsonIgnore
    private Collection<Facturecommandefournisseur> facturecommandefournisseurCollection;
    @OneToMany(mappedBy = "produitId", fetch = FetchType.LAZY)
    @JsonIgnore
    private Collection<Inventaire> inventaireCollection;
    @JoinColumn(name = "Categorie", referencedColumnName = "id")
    @ManyToOne
    private Categories categories;
    @JoinColumn(name = "Specifiqueid", referencedColumnName = "id")
    @ManyToOne
    private Specifique specifiqueid;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "artticleId", fetch = FetchType.LAZY)
    @JsonIgnore
    private Collection<Specificationarticles> specificationarticlesCollection;
    @OneToMany(mappedBy = "produit", fetch = FetchType.LAZY)
    @JsonIgnore
    private Collection<Commande> commandeCollection;
    @OneToMany(mappedBy = "produit", fetch = FetchType.LAZY)
    @JsonIgnore
    private Collection<PointVente> pointventeCollection;

    @Transient
    private BigDecimal prixVente = BigDecimal.ZERO;
    @Transient
    private BigDecimal prixAchat = BigDecimal.ZERO;
    @Transient
    private BigInteger quantite;
    @Transient
    private Boutique boutique;

    public Produit() {
    }

    public Produit(Long id) {
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

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Boolean getPrixVenteModifiable() {
        return prixVenteModifiable;
    }

    public void setPrixVenteModifiable(Boolean prixVenteModifiable) {
        this.prixVenteModifiable = prixVenteModifiable;
    }

    public Boolean getDeletes() {
        return deletes;
    }

    public void setDeletes(Boolean deletes) {
        this.deletes = deletes;
    }

    public Boolean getPacquets() {
        return pacquets;
    }

    public void setPacquets(Boolean pacquets) {
        this.pacquets = pacquets;
    }

    public BigDecimal getQuantiteByPacquet() {
        return quantiteByPacquet;
    }

    public void setQuantiteByPacquet(BigDecimal quantiteByPacquet) {
        this.quantiteByPacquet = quantiteByPacquet;
    }

    public Date getDateDelete() {
        return dateDelete;
    }

    public void setDateDelete(Date dateDelete) {
        this.dateDelete = dateDelete;
    }

    public String getDeleteUser() {
        return deleteUser;
    }

    public void setDeleteUser(String deleteUser) {
        this.deleteUser = deleteUser;
    }

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }

    public Collection<Produitachat> getProduitachatCollection() {
        return produitachatCollection;
    }

    public void setProduitachatCollection(Collection<Produitachat> produitachatCollection) {
        this.produitachatCollection = produitachatCollection;
    }

    public Collection<Prixclient> getPrixclientCollection() {
        return prixclientCollection;
    }

    public void setPrixclientCollection(Collection<Prixclient> prixclientCollection) {
        this.prixclientCollection = prixclientCollection;
    }

    public Collection<Facturecommandefournisseur> getFacturecommandefournisseurCollection() {
        return facturecommandefournisseurCollection;
    }

    public void setFacturecommandefournisseurCollection(Collection<Facturecommandefournisseur> facturecommandefournisseurCollection) {
        this.facturecommandefournisseurCollection = facturecommandefournisseurCollection;
    }

    public Collection<Inventaire> getInventaireCollection() {
        return inventaireCollection;
    }

    public void setInventaireCollection(Collection<Inventaire> inventaireCollection) {
        this.inventaireCollection = inventaireCollection;
    }

    public Categories getCategorie() {
        return categories;
    }

    public void setCategorie(Categories categorie) {
        this.categories = categorie;
    }

    public Specifique getSpecifiqueid() {
        return specifiqueid;
    }

    public void setSpecifiqueid(Specifique specifiqueid) {
        this.specifiqueid = specifiqueid;
    }

    public Collection<Specificationarticles> getSpecificationarticlesCollection() {
        return specificationarticlesCollection;
    }

    public void setSpecificationarticlesCollection(Collection<Specificationarticles> specificationarticlesCollection) {
        this.specificationarticlesCollection = specificationarticlesCollection;
    }

    public Collection<Commande> getCommandeCollection() {
        return commandeCollection;
    }

    public void setCommandeCollection(Collection<Commande> commandeCollection) {
        this.commandeCollection = commandeCollection;
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
        if (!(object instanceof Produit)) {
            return false;
        }
        Produit other = (Produit) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mproduits.model.Produit[ id=" + id + " ]";
    }

    public Collection<Taxeproduit> getTaxproduitCollection() {
        return taxproduitCollection;
    }

    public void setTaxproduitCollection(Collection<Taxeproduit> taxproduitCollection) {
        this.taxproduitCollection = taxproduitCollection;
    }

}
