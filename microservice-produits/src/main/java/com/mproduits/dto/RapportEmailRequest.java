/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 *
 * @author USER01
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RapportEmailRequest {
    
    @NotNull (message = "L'ID du client est obligatoire")
    private Long clientId;
    
    @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime dateDebut;
    
    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime dateFin;
    
    @NotNull(message = "L'adresse email est obligatoire")
    @Email(message = "L'adresse email doit être valide")
    private String email;    
}
