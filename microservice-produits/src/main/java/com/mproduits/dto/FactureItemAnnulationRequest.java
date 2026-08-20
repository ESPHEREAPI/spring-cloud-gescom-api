package com.mproduits.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FactureItemAnnulationRequest {

    @NotBlank(message = "Le motif d'annulation est obligatoire")
    @Size(max = 1000, message = "Le motif ne peut pas dépasser 1000 caractères")
    private String motif;
}
