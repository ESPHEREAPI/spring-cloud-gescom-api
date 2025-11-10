/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

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
public class RecuPaiementDTO {

    private String numeroRecu;
    private Date dateEmission;

    // Versement
    private VersementResponse versement;
    private List<Long> versementIds;

    // Facture
    private Long factureId;
    private String factureNumero;
    private Date factureDate;
    private BigDecimal factureTotalTtc;
    private BigDecimal factureSoldeRestant;
    private BigDecimal montant;
    private String montantEnLettres;

    private String modePaiement;
    private String referencePaiement;
    private Date dateVersement;
    // Client
    private Long clientId;
    private String clientNom;
    private String clientAdresse;
    private String clientTelephone;
    private String clientEmail;

    // Entreprise (émetteur)
    private String entrepriseNom;
    private String entrepriseAdresse;
    private String entrepriseTelephone;
    private String entrepriseEmail;
    private Long versementId;

}
