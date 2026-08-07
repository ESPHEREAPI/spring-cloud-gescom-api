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
import jakarta.persistence.JoinColumns;
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
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "commande_easy")
@NamedQueries({
    @NamedQuery(name = "Commande.findAll", query = "SELECT c FROM Commande c"),
    @NamedQuery(name = "Commande.findById", query = "SELECT c FROM Commande c WHERE c.id = :id"),
    @NamedQuery(name = "Commande.findByDateReception", query = "SELECT c FROM Commande c WHERE c.dateReception = :dateReception"),
    @NamedQuery(name = "Commande.findByEntreeProduit", query = "SELECT c FROM Commande c WHERE c.entreeProduit = :entreeProduit"),
    // @NamedQuery(name = "Commande.findByFondCuve", query = "SELECT c FROM Commande c WHERE c.fondCuve = :fondCuve"),
    //@NamedQuery(name = "Commande.findByFondCuveEntre", query = "SELECT c FROM Commande c WHERE c.fondCuveEntre = :fondCuveEntre"),
    //@NamedQuery(name = "Commande.findByFondCuveSorti", query = "SELECT c FROM Commande c WHERE c.fondCuveSorti = :fondCuveSorti"),
    @NamedQuery(name = "Commande.findByNumeroRepartition", query = "SELECT c FROM Commande c WHERE c.numeroRepartition = :numeroRepartition"),
    @NamedQuery(name = "Commande.findBySoortiProduit", query = "SELECT c FROM Commande c WHERE c.sortiProduit = :sortiProduit"),
    @NamedQuery(name = "Commande.findByStockFinalTheorie", query = "SELECT c FROM Commande c WHERE c.stockFinalTheorie = :stockFinalTheorie"),
    @NamedQuery(name = "Commande.findByStockInitial", query = "SELECT c FROM Commande c WHERE c.stockInitial = :stockInitial"),
    @NamedQuery(name = "Commande.findByPrixAchat", query = "SELECT c FROM Commande c WHERE c.prixAchat = :prixAchat"),
    @NamedQuery(name = "Commande.findByPrixVente", query = "SELECT c FROM Commande c WHERE c.prixVente = :prixVente")})
public class Commande implements Serializable {

   

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "date_reception")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateReception;
    
    @Column(name = "Entree_Produit")
    private BigDecimal entreeProduit;

    @Column(name = "numero_Repartition")
    private String numeroRepartition;
    @Column(name = "soorti_Produit")
    private BigDecimal sortiProduit;
    @Column(name = "stock_final_theorie")
    private BigDecimal stockFinalTheorie;
    @Column(name = "stock_initial")
    private BigDecimal stockInitial;
    @Column(name = "prixachat")
    private BigDecimal prixAchat;
    @Column(name = "prixvente")
    private BigDecimal prixVente;

   
    @JoinColumn(name = "depotid", referencedColumnName = "id")
    @ManyToOne
    private Magasin magasinid;
    @JoinColumns({
        @JoinColumn(name = "Anneeid", referencedColumnName = "Anneeid"),
        @JoinColumn(name = "compagnie_id", referencedColumnName = "compagnie_id")})
    @ManyToOne(optional = false)
    private Entreprise entreprise;
    @JoinColumn(name = "produitid", referencedColumnName = "id")
    @ManyToOne
    private Produit produit;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "commande")
        @JsonIgnore
   // private Collection<Livraison_old> livraisonCollection;

    @Transient
    private BigDecimal quantite;
    @Transient
    Fournisseur fournisseurid;
    @Transient
    String usercreate;

  
}
