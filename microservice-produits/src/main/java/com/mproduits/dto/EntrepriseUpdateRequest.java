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
 *
 * @author USER01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
class EntrepriseUpdateRequest {

    @NotNull(message = "L'année est obligatoire")
    private Integer anneeId;

    @NotNull(message = "L'employeur est obligatoire")
    private Long employeurId;

    @Size(max = 50)
    private String directeur;

    @Size(max = 50)
    private String activite;

    @Size(max = 50)
    private String conventionCollective;

    @Size(max = 200)
    private String siteWeb;

    private Boolean actif;

    @Size(max = 200)
    private String typeResponsable;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateCreation;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFinLicense;
    
}
