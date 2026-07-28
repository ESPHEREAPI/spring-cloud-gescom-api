/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour l'entité MagasinFournisseur
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MagasinFournisseurDTO {
    
    // Clé composite
    @NotNull(message = "Le magasin est obligatoire")
    private Long depotId;
    
    @NotNull(message = "Le fournisseur est obligatoire")
    private Long fournisseurId;
    
    // Informations du magasin
    private String magasinCode;
    private String magasinLibelle;
    private String boutiqueNom;
    
    // Informations du fournisseur
    private String fournisseurCode;
    private String fournisseurNom;
    private String fournisseurEmail;
    private String fournisseurTel;
    private String fournisseurVille;
    
    // Informations entreprise (optionnel)
    private int anneeId;
    private Long employeurId;
    private String entrepriseNom;
    
    // Métadonnées
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateCreation;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateModification;
    
    private String creePar;
    private String modifiePar;
    
    // Informations calculées
    private Integer nombreCommandes;
    private Double montantTotal;
    private String statut; // ACTIF, INACTIF, EN_ATTENTE
    
}
