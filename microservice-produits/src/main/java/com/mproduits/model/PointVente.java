/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author USER01
 */
@Entity

@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pointvente_easy")
@NamedQueries({
    @NamedQuery(name = "PointVente.findAll", query = "SELECT p FROM PointVente p"),
    @NamedQuery(name = "PointVente.findById", query = "SELECT p FROM PointVente p WHERE p.id = :id"),
    @NamedQuery(name = "PointVente.findByDateReception", query = "SELECT p FROM PointVente p WHERE p.dateReception = :dateReception"),
    @NamedQuery(name = "PointVente.findByEntreeProduit", query = "SELECT p FROM PointVente p WHERE p.entreeProduit = :entreeProduit"),
    @NamedQuery(name = "PointVente.findByPerte", query = "SELECT p FROM PointVente p WHERE p.perte = :perte"),
    //@NamedQuery(name = "PointVente.findByFondCuveEntre", query = "SELECT p FROM PointVente p WHERE p.fondCuveEntre = :fondCuveEntre"),
    //@NamedQuery(name = "PointVente.findByFondCuveSorti", query = "SELECT p FROM PointVente p WHERE p.fondCuveSorti = :fondCuveSorti"),
    @NamedQuery(name = "PointVente.findBySoortiProduit", query = "SELECT p FROM PointVente p WHERE p.sortiProduit = :sortiProduit"),
    @NamedQuery(name = "PointVente.findByStockFinalTheorie", query = "SELECT p FROM PointVente p WHERE p.stockFinalTheorie = :stockFinalTheorie"),
    @NamedQuery(name = "PointVente.findByStockInitial", query = "SELECT p FROM PointVente p WHERE p.stockInitial = :stockInitial")})
@Data
public class PointVente implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "date_reception")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateReception;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "Entree_Produit")
    private BigDecimal entreeProduit;
    @Column(name = "perte")
    private BigDecimal perte;

    @Column(name = "soorti_Produit")
    private BigDecimal sortiProduit;
    @Column(name = "stock_final_theorie")
    private BigDecimal stockFinalTheorie;
    @Column(name = "stock_initial")
    private BigDecimal stockInitial;
    @OneToMany(mappedBy = "pointVente", fetch = FetchType.LAZY)
    @JsonIgnore
    private Collection<PrixArticles> prixarticlesCollection;
    @JoinColumn(name = "Boutiqueid", referencedColumnName = "id")
    @ManyToOne
    private Boutique boutique;
    @JoinColumn(name = "depotid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Magasin depotId;
     @JsonIgnore
    @JoinColumns({
        @JoinColumn(name = "Anneeid", referencedColumnName = "Anneeid"),
        @JoinColumn(name = "Employeurid", referencedColumnName = "Employeurid")})
    @ManyToOne(optional = false)
    private Entreprise entreprise;

    @JoinColumn(name = "produitid", referencedColumnName = "id")
    @ManyToOne
    private Produit produit;
//    @OneToMany(mappedBy = "poinVente", fetch = FetchType.LAZY)
//    @JsonIgnore
//    private Collection<Livraison_old> livraisonCollection;

    public PointVente(Long id) {
        this.id = id;
    }

}
