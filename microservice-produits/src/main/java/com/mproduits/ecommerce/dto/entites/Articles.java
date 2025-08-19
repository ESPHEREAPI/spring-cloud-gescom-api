/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.ecommerce.dto.entites;

import java.io.Serializable;
import java.math.BigDecimal;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "articles")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Articles.findAll", query = "SELECT a FROM Articles a"),
    @NamedQuery(name = "Articles.findById", query = "SELECT a FROM Articles a WHERE a.id = :id"),
    @NamedQuery(name = "Articles.findByActif", query = "SELECT a FROM Articles a WHERE a.actif = :actif"),
    @NamedQuery(name = "Articles.findByQuantite", query = "SELECT a FROM Articles a WHERE a.quantite = :quantite"),
    @NamedQuery(name = "Articles.findByPrixUnitaire", query = "SELECT a FROM Articles a WHERE a.prixUnitaire = :prixUnitaire"),
    @NamedQuery(name = "Articles.findByLibelle", query = "SELECT a FROM Articles a WHERE a.libelle = :libelle"),
    @NamedQuery(name = "Articles.findByReference", query = "SELECT a FROM Articles a WHERE a.reference = :reference"),
//    @NamedQuery(name = "Articles.findByPhotoName", query = "SELECT a FROM Articles a WHERE a.photoName = :photoName"),
    @NamedQuery(name = "Articles.findByCategorie", query = "SELECT a FROM Articles a WHERE a.categorie = :categorie"),
    @NamedQuery(name = "Articles.findByAnneeId", query = "SELECT a FROM Articles a WHERE a.anneeId = :anneeId")})
public class Articles implements Serializable {

    private static final long serialVersionUID = 1L;
     @Id
    @Basic(optional = false)
    @Column(name = "Id")
    private long id;//id prix articles
    @Basic(optional = false)
    @Column(name = "actif")
    private boolean actif;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "quantite")
    private BigDecimal quantite;
    @Column(name = "prix_unitaire")
    private BigDecimal prixUnitaire;
    @Column(name = "libelle")
    private String libelle;
    @Column(name = "reference")
    private String reference;
    @Column(name = "photo_name")
    private String photoName;
    @Basic(optional = false)
    @Column(name = "categorie")
    private long categorie;
    @Basic(optional = false)
    @Column(name = "annee_id")
    private int anneeId;
     @Column(name = "produit")
    private long produit;
     @Transient
     int quantite_caddy=1;

    public Articles() {
        quantite_caddy=1;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean getActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public BigDecimal getQuantite() {
        return quantite;
    }

    public void setQuantite(BigDecimal quantite) {
        this.quantite = quantite;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
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

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }

    public long getCategorie() {
        return categorie;
    }

    public void setCategorie(long categorie) {
        this.categorie = categorie;
    }

    public int getAnneeId() {
        return anneeId;
    }

    public void setAnneeId(int anneeId) {
        this.anneeId = anneeId;
    }

    public long getProduit() {
        return produit;
    }

    public void setProduit(long produit) {
        this.produit = produit;
    }

    public int getQuantite_caddy() {
        return quantite_caddy;
    }

    public void setQuantite_caddy(int quantite_caddy) {
        this.quantite_caddy = quantite_caddy;
    }
    
    
}
