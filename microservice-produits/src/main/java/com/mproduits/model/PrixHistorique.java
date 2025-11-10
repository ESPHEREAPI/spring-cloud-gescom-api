/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 *
 * @author USER01
 */
@Entity
@Table(name = "prix_historique", indexes = {
    @Index(name = "idx_produit_date", columnList = "produit_id, date_creation DESC"),
    @Index(name = "idx_point_vente", columnList = "point_vente_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrixHistorique implements Serializable{
     private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_vente_id", nullable = false)
    private PointVente pointVente;
    
    @NotNull(message = "Prix vente net requis")
    @DecimalMin(value = "0.0", inclusive = false, message = "Prix doit être > 0")
    @Column(name = "prix_vente_net", precision = 12, scale = 2, nullable = false)
    private BigDecimal prixVenteNet;
    
    @NotNull(message = "Taux TVA requis")
    @Min(value = 0, message = "TVA min: 0%")
    @Max(value = 100, message = "TVA max: 100%")
    @Column(name = "taux_tva", precision = 5, scale = 2, nullable = false)
    private BigDecimal tauxTVA;
    
    // Calculé: prixVenteNet * (1 + tauxTVA/100)
    @Column(name = "prix_vente_ttc", precision = 12, scale = 2, nullable = false)
    private BigDecimal prixVenteTTC;
    
    @DecimalMin(value = "0.0", message = "Remise min: 0%")
    @DecimalMax(value = "50.0", message = "Remise max: 50%")
    @Column(name = "remise_percent", precision = 5, scale = 2)
    private BigDecimal remisePercent = BigDecimal.ZERO;
    
    /** Actif: true = prix courant applicable */
    @Column(nullable = false)
    private boolean actif = true;
    
    @Column(nullable = false)
    private String usernameCreate;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;
    
    /**
     * Calcule et valide le prix TTC = PrixNet * (1 + TVA/100)
     * @PrePersist et @PreUpdate
     */
    @PrePersist
    @PreUpdate
    public void calculerPrixTTC() {
        if (prixVenteNet != null && tauxTVA != null) {
            BigDecimal coeff = BigDecimal.ONE.add(tauxTVA.divide(
                new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP));
            this.prixVenteTTC = prixVenteNet.multiply(coeff)
                .setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }
}
