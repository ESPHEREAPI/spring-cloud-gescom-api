///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.mproduits.model;
//
//import jakarta.persistence.Basic;
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
//import jakarta.persistence.NamedQueries;
//import jakarta.persistence.NamedQuery;
//import jakarta.persistence.Table;
//import jakarta.persistence.Temporal;
//import jakarta.persistence.TemporalType;
//import java.io.Serializable;
//import java.math.BigDecimal;
//import java.util.Date;
//
///**
// *
// * @author USER01
// */
//@Entity
//@Table(name = "livraison")
//@NamedQueries({
//    @NamedQuery(name = "Livraison.findAll", query = "SELECT l FROM Livraison l"),
//    @NamedQuery(name = "Livraison.findById", query = "SELECT l FROM Livraison l WHERE l.id = :id"),
//    @NamedQuery(name = "Livraison.findByDateLivraison", query = "SELECT l FROM Livraison l WHERE l.dateLivraison = :dateLivraison"),
//    @NamedQuery(name = "Livraison.findByBondechargement", query = "SELECT l FROM Livraison l WHERE l.bondechargement = :bondechargement"),
//    @NamedQuery(name = "Livraison.findByLibelle", query = "SELECT l FROM Livraison l WHERE l.libelle = :libelle"),
//    @NamedQuery(name = "Livraison.findByUnite", query = "SELECT l FROM Livraison l WHERE l.unite = :unite"),
//    @NamedQuery(name = "Livraison.findByQuantite", query = "SELECT l FROM Livraison l WHERE l.quantite = :quantite"),
//    @NamedQuery(name = "Livraison.findByRemise", query = "SELECT l FROM Livraison l WHERE l.remise = :remise"),
//    @NamedQuery(name = "Livraison.findByNumFacture", query = "SELECT l FROM Livraison l WHERE l.numFacture = :numFacture")})
//public class Livraison_old implements Serializable {
//
//    private static final long serialVersionUID = 1L;
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Basic(optional = false)
//    @Column(name = "id")
//    private Long id;
//    @Column(name = "date_Livraison")
//    @Temporal(TemporalType.TIMESTAMP)
//    private Date dateLivraison;
//    @Column(name = "Bon_de_chargement")
//    private String bondechargement;
//    @Column(name = "libelle")
//    private String libelle;
//    @Column(name = "unite")
//    private String unite;
//    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
//    @Column(name = "quantite")
//    private BigDecimal quantite;
//    @Column(name = "remise")
//    private BigDecimal remise;
//    @Column(name = "numFacture")
//    private String numFacture;
//    @JoinColumn(name = "Commandeid", referencedColumnName = "id")
//    @ManyToOne(optional = false)
//    private Commande commande;
//    @JoinColumn(name = "Moisid", referencedColumnName = "id")
//    @ManyToOne
//    private Mois mois;
//    @JoinColumn(name = "poinVente", referencedColumnName = "id")
//    @ManyToOne
//    private PointVente poinVente;
//    @JoinColumn(name = "PrixClientid", referencedColumnName = "id")
//    @ManyToOne(optional = false)
//    private Prixclient prixClientid;
//
//    public Livraison_old() {
//    }
//
//    public Livraison_old(Long id) {
//        this.id = id;
//    }
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public Date getDateLivraison() {
//        return dateLivraison;
//    }
//
//    public void setDateLivraison(Date dateLivraison) {
//        this.dateLivraison = dateLivraison;
//    }
//
//    public String getBondechargement() {
//        return bondechargement;
//    }
//
//    public void setBondechargement(String bondechargement) {
//        this.bondechargement = bondechargement;
//    }
//
//    public String getLibelle() {
//        return libelle;
//    }
//
//    public void setLibelle(String libelle) {
//        this.libelle = libelle;
//    }
//
//    public String getUnite() {
//        return unite;
//    }
//
//    public void setUnite(String unite) {
//        this.unite = unite;
//    }
//
//    public BigDecimal getQuantite() {
//        return quantite;
//    }
//
//    public void setQuantite(BigDecimal quantite) {
//        this.quantite = quantite;
//    }
//
//    public BigDecimal getRemise() {
//        return remise;
//    }
//
//    public void setRemise(BigDecimal remise) {
//        this.remise = remise;
//    }
//
//    public String getNumFacture() {
//        return numFacture;
//    }
//
//    public void setNumFacture(String numFacture) {
//        this.numFacture = numFacture;
//    }
//
//    public Commande getCommandeid() {
//        return commande;
//    }
//
//    public void setCommandeid(Commande commandeid) {
//        this.commande = commandeid;
//    }
//
//    public Mois getMois() {
//        return mois;
//    }
//
//    public void setMois(Mois moisId) {
//        this.mois = moisId;
//    }
//
//    public PointVente getPoinVente() {
//        return poinVente;
//    }
//
//    public void setPoinVente(PointVente poinVente) {
//        this.poinVente = poinVente;
//    }
//
//    public Prixclient getPrixClientid() {
//        return prixClientid;
//    }
//
//    public void setPrixClientid(Prixclient prixClientid) {
//        this.prixClientid = prixClientid;
//    }
//
//    @Override
//    public int hashCode() {
//        int hash = 0;
//        hash += (id != null ? id.hashCode() : 0);
//        return hash;
//    }
//
//    @Override
//    public boolean equals(Object object) {
//        // TODO: Warning - this method won't work in the case the id fields are not set
//        if (!(object instanceof Livraison_old)) {
//            return false;
//        }
//        Livraison_old other = (Livraison_old) object;
//        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    public String toString() {
//        return "com.mproduits.model.Livraison[ id=" + id + " ]";
//    }
//    
//}
