/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 *
 * @author USER01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntrepriseResponse {
    private Integer anneeId;
    private Long employeurId;
    private String directeur;
    private String activite;
    private String conventionCollective;
    private String siteWeb;
    private Boolean actif;
    private String typeResponsable;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateCreation;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFinLicense;

    // Données de l'année
    private AnneeInfo annee;

    // Données de l'employeur
    private EmployeurInfo employeur;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnneeInfo {
        private Integer id;
        private String code;
        private String libelle;
        private Boolean actif;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeurInfo {
        private Long id;
        private String matricule;
        private String societe;
        private String abreviation;
        private String telephone;
        private String email;
        private String adresse;
        private String ville;
    }
    
}
