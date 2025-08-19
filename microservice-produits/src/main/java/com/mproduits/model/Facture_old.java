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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author USER01
 */
@Entity
@Table(name = "facture_old")

public class Facture_old implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "date_reception")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateReception;
    @Lob
    @Column(name = "facture")
    private byte[] facture;
    @Column(name = "formatFacture")
    private String formatFacture;
    @Column(name = "formatVoyage")
    private String formatVoyage;
    @Column(name = "libelle")
    private String libelle;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "montant")
    private BigDecimal montant;
    @Column(name = "numero_facture")
    private String numeroFacture;
    @Lob
    @Column(name = "voyage")
    private byte[] voyage;

    @JoinColumns({
        @JoinColumn(name = "AnneeId", referencedColumnName = "AnneeId"),
        @JoinColumn(name = "EmployeurId", referencedColumnName = "EmployeurId")})
    @ManyToOne(optional = false)
    private Entreprise entreprise;
    @JoinColumn(name = "fournisseurId", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Fournisseur fournisseurId;
    @JoinColumn(name = "Quinzaine", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Quinzaine quinzaine;

  

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDateReception() {
        return dateReception;
    }

    public void setDateReception(Date dateReception) {
        this.dateReception = dateReception;
    }

    public byte[] getFacture() {
        return facture;
    }

    public void setFacture(byte[] facture) {
        this.facture = facture;
    }

    public String getFormatFacture() {
        return formatFacture;
    }

    public void setFormatFacture(String formatFacture) {
        this.formatFacture = formatFacture;
    }

    public String getFormatVoyage() {
        return formatVoyage;
    }

    public void setFormatVoyage(String formatVoyage) {
        this.formatVoyage = formatVoyage;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getNumeroFacture() {
        return numeroFacture;
    }

    public void setNumeroFacture(String numeroFacture) {
        this.numeroFacture = numeroFacture;
    }

    public byte[] getVoyage() {
        return voyage;
    }

    public void setVoyage(byte[] voyage) {
        this.voyage = voyage;
    }

   

    public Entreprise getEntreprise() {
        return entreprise;
    }

    public void setEntreprise(Entreprise entreprise) {
        this.entreprise = entreprise;
    }

    public Fournisseur getFournisseurId() {
        return fournisseurId;
    }

    public void setFournisseurId(Fournisseur fournisseurId) {
        this.fournisseurId = fournisseurId;
    }

    public Quinzaine getQuinzaine() {
        return quinzaine;
    }

    public void setQuinzaine(Quinzaine quinzaine) {
        this.quinzaine = quinzaine;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

   

    @Override
    public String toString() {
        return "com.mproduits.model.Facture[ id=" + id + " ]";
    }
    
}
