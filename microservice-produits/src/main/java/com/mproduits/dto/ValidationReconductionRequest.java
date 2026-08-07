/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la requête de validation de reconduction.
 * 
 * @author MProduits Dev Team
 * @version 1.0
 * @since 2025-01-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationReconductionRequest {
    
    @NotNull(message = "L'année source est obligatoire")
    @Positive(message = "L'année source doit être positive")
    private Integer anneeSourceId;

    @NotNull(message = "L'année cible est obligatoire")
    @Positive(message = "L'année cible doit être positive")
    private Integer anneeCibleId;
}
