/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
/**
 *
 * @author USER01
 */
@Entity
@Table(name = "facture_item_snapshot")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactureItemSnapshot implements Serializable {
     private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_id", nullable = false)
    private Facture facture;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;
    
    @Column(nullable = false)
    private Integer quantite;
    
    // Snapshot du prix au moment de la facturation
    @Column(name = "prix_unitaire_ht", precision = 12, scale = 2, nullable = false)
    private BigDecimal prixUnitaireHT;
    
    @Column(name = "taux_tva", precision = 5, scale = 2, nullable = false)
    private BigDecimal tauxTVA;
    
    @Column(name = "prix_unitaire_ttc", precision = 12, scale = 2, nullable = false)
    private BigDecimal prixUnitaireTTC;
    
    @Column(name = "remise_percent", precision = 5, scale = 2)
    private BigDecimal remisePercent = BigDecimal.ZERO;
    
    // Total ligne = (PU - remise) * quantité
    @Column(name = "montant_ht", precision = 12, scale = 2, nullable = false)
    private BigDecimal montantHT;
    
    @Column(name = "montant_ttc", precision = 12, scale = 2, nullable = false)
    private BigDecimal montantTTC;
    
    /** Code produit au moment de la facturation (traçabilité) */
    @Column(name = "produit_code_snapshot")
    private String produitCodeSnapshot;
    
    /** Libellé produit au moment de la facturation */
    @Column(name = "produit_libelle_snapshot")
    private String produitLikelleSnapshot;
}
