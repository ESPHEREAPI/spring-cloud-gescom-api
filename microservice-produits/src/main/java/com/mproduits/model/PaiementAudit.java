/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.model;
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
@Table(name = "paiement_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaiementAudit implements Serializable{
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "versement_id", nullable = false)
    private VersementClient versement;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_id", nullable = false)
    private Facture facture;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    
    @Column(nullable = false)
    private BigDecimal montantVersement;
    
    @Column(nullable = false)
    private BigDecimal montantFacture;
    
    @Column(nullable = false)
    private BigDecimal soldeRestant;
    
    @Column(nullable = false)
    private String modePaiement;
    
    @Column(nullable = false)
    private String statut; // EN_ATTENTE, VALIDE, REJETE
    
    @Column(nullable = false)
    private String usernameCreate;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;
}
