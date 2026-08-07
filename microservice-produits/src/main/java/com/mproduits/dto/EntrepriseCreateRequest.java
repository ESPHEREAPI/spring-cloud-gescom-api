/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO pour créer une nouvelle entreprise
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntrepriseCreateRequest {

    @NotNull(message = "L'année est obligatoire")
    private Integer anneeId;

    @Size(max = 50, message = "Le nom du directeur ne doit pas dépasser 50 caractères")
    private String directeur;

    @NotNull(message = "L'activité est obligatoire")
    @Size(max = 50, message = "L'activité ne doit pas dépasser 50 caractères")
    private String activite;

    @NotNull(message = "La convention collective est obligatoire")
    @Size(max = 50, message = "La convention collective ne doit pas dépasser 50 caractères")
    private String conventionCollective;

    @Size(max = 200, message = "Le site web ne doit pas dépasser 200 caractères")
    private String siteWeb;

    @NotNull(message = "Le statut actif est obligatoire")
    private Boolean actif;

    @Size(max = 200, message = "Le type de responsable ne doit pas dépasser 200 caractères")
    private String typeResponsable;

    @NotNull(message = "La date de création est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateCreation;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFinLicense;
    
}
