/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mproduits.enums.TypePromotion;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author USER01
 */
@Entity
@Data
//@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "prixarticles_easy")
@NamedQueries({
    @NamedQuery(name = "PrixArticles.findAll", query = "SELECT p FROM PrixArticles p"),
    @NamedQuery(name = "PrixArticles.findById", query = "SELECT p FROM PrixArticles p WHERE p.id = :id"),
    @NamedQuery(name = "PrixArticles.findByDateCreation", query = "SELECT p FROM PrixArticles p WHERE p.dateCreation = :dateCreation"),
    @NamedQuery(name = "PrixArticles.findByActif", query = "SELECT p FROM PrixArticles p WHERE p.actif = :actif"),
    @NamedQuery(name = "PrixArticles.findByPrixVenteNet", query = "SELECT p FROM PrixArticles p WHERE p.prixVenteNet = :prixVenteNet"),
    @NamedQuery(name = "PrixArticles.findByPrixVenteTTC", query = "SELECT p FROM PrixArticles p WHERE p.prixVenteTTC = :prixVenteTTC"),
    @NamedQuery(name = "PrixArticles.findByRemise", query = "SELECT p FROM PrixArticles p WHERE p.remise = :remise"),
    @NamedQuery(name = "PrixArticles.findByTva", query = "SELECT p FROM PrixArticles p WHERE p.tva = :tva"),
    @NamedQuery(name = "PrixArticles.findByDateEnregistrement", query = "SELECT p FROM PrixArticles p WHERE p.dateEnregistrement = :dateEnregistrement")})
public class PrixArticles implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "datecreation")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreation;
    @Basic(optional = false)
    @Column(name = "actif")
    private boolean actif;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "prixventenet")
    private BigDecimal prixVenteNet;
    @Column(name = "prixventettc")
    private BigDecimal prixVenteTTC;
    @Column(name = "remise")
    private BigDecimal remise;
    @Column(name = "tva")
    private BigDecimal tva;
    @Column(name = "dateenregistrement")
    @Temporal(TemporalType.DATE)
    private Date dateEnregistrement;
     @Column(name = "datemiseajour")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datemiseajour;
//    @OneToMany(cascade = CascadeType.ALL, mappedBy = "prixArticlesId")
//    private Collection<Pertearticles> pertearticlesCollection;
    @JoinColumns({
        @JoinColumn(name = "Anneeid", referencedColumnName = "Anneeid"),
        @JoinColumn(name = "compagnie_id", referencedColumnName = "compagnie_id")})
    @ManyToOne(optional = false)
     @JsonIgnore
    private Entreprise entreprise;
    @JoinColumn(name = "pointventeid", referencedColumnName = "id")
    @ManyToOne
    private PointVente pointVente;
    // Promotion/solde par boutique (page de vente publique) - reutilise le
    // champ "remise" existant pour le pourcentage. Bornes de dates
    // optionnelles pour une promotion qui expire automatiquement.
    @Enumerated(EnumType.STRING)
    @Column(name = "type_promotion")
    private TypePromotion typePromotion = TypePromotion.AUCUNE;
    @Column(name = "date_debut_promo")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateDebutPromo;
    @Column(name = "date_fin_promo")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateFinPromo;
//    @OneToMany(cascade = CascadeType.ALL, mappedBy = "prixArticles")
//    private Collection<Barcodeproduit> barcodeproduitCollection;
    @Transient
    private BigInteger quantite;
    @Transient
    String codeBar;

//    @PrePersist
//    protected void onCreate() {
//        dateCreation = new Date();
//      
//        calculerPrixTTC();
//    }
    
//    @PreUpdate
//    protected void onUpdate() {
//        dateEnregistrement =new Date();
//        calculerPrixTTC();
//    }
//    
//    private void calculerPrixTTC() {
//        if (prixVenteNet != null && tva != null) {
//            this.prixVenteTTC = this.prixVenteNet * (1 + tva / 100);
//        }
//    }

    /** Promotion/solde en cours a l'instant present, compte tenu des bornes de dates optionnelles. */
    public boolean isPromotionActive() {
        if (typePromotion == null || typePromotion == TypePromotion.AUCUNE) {
            return false;
        }
        Date maintenant = new Date();
        if (dateDebutPromo != null && maintenant.before(dateDebutPromo)) {
            return false;
        }
        if (dateFinPromo != null && maintenant.after(dateFinPromo)) {
            return false;
        }
        return true;
    }

}
