/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.mproduits.enums.ModePaiement;
import com.mproduits.enums.StatutFacture;
import com.mproduits.enums.StatutVersement;
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
public class VersementResponse {
    private Long id;
    private String numeroVersement;
    private Date dateVersement;
    private Date dateEnregistrement;
    private Date dateValidation;
    private BigDecimal montant;
    
    // Mode de paiement
    private ModePaiement modePaiement;
    private String modePaiementLibelle;
    private String referencePaiement;
    private String banque;
    private String numeroCompte;
    
    // Statut
    private StatutVersement statut;
    private Integer tentatives;
    
    // Facture
    private Long factureId;
    private String factureNumero;
    private BigDecimal factureTotalTtc;
    private BigDecimal factureSoldeRestant;
    private VersementFacture facture;
    
    // Client
    private Long clientId;
    private String clientNom;
    private String clientEmail;
    
    // Informations complémentaires
    private String remarques;
    private String motifAnnulation;
    private String recuPaiement;
    
    // Traçabilité
    private Date dateCreation;
    private String usernameCreate;
    private String usernameValidation;
    private String usernameAnnulation;
}
