/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;
import com.mproduits.enums.MovementType;
import jakarta.persistence.*;
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
@Table(name = "stock_movement")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class StockMovement implements Serializable {
     private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** Type de mouvement: ENTREE, SORTIE_VENTE, RETOUR, INVENTAIRE, AJUSTEMENT */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType typeMouvement;
    
    @Column(nullable = false)
    private BigDecimal quantite,stockAvant;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_vente_id", nullable = false)
    private PointVente pointVente;
    
    // Référence vers la facture si mouvement lié à facturation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_id")
    private Facture facture;
    
    @Column(columnDefinition = "TEXT")
    private String motif,reference; // Raison du mouvement
    
    @Column(nullable = false)
    private String usernameCreate;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;
    
  
    
}
