/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.enums.ModePaiement;
import com.mproduits.enums.StatutFacture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;
import java.math.BigDecimal;

/**
 *
 * @author USER01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactureResponse {
    
    private Long id;
    private String numeroFacture;
    private Date dateFacture;
    private Date dateEcheance;
    private Date dateValidation;
    private Date datePaiementComplet;
    
    // Montants
    private BigDecimal totalHt;
    private BigDecimal totalRemise;
    private BigDecimal totalTVA;
    private BigDecimal totalTtc;
    private BigDecimal montantPaye;
    private BigDecimal soldeRestant;
    
    // Statut
    private StatutFacture statut;
    private String statutLibelle;
    
    // Client
    private Long clientId;
    private String clientNom;
    private String clientEmail;
    private String clientTelephone;
    
    // Devis source
    private Long devisId;
    private String devisNumero;
    
    // Paiement
    private ModePaiement modePaiementDefaut;
    private Integer delaiPaiementJours;
    private BigDecimal penaliteRetard;
    private BigDecimal montantPenalite;
    
    // Articles
    private List<FactureItemResponse> items;
    
    // Versements
    private List<VersementSummary> versements;
    
    // Informations complémentaires
    private String remarques;
    private String conditionsPaiement;
    private String motifAnnulation;
    private String referenceExterne;
    
    // État
    private Boolean envoyeeClient;
    private Date dateEnvoiClient;
    private Integer nombreRelances;
    private Date dateDerniereRelance;
    private Boolean enRetard;
    private Long joursRetard;
    private Long joursAvantEcheance;
    private BigDecimal pourcentagePaye;
    
    // Traçabilité
    private Date dateCreation;
    private Date dateModification;
    private String usernameCreate;
    private String usernameValidation;
    private ClientDto client;
}

