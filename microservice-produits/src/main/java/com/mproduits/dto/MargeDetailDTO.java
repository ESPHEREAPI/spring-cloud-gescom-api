package com.mproduits.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mproduits.model.Charge;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Marge reelle d'une boutique sur une periode = total des ressources
 * (caisse + versements clients + ressources manuelles) moins le total des
 * charges - avec le detail complet, pas seulement le solde net. A ne pas
 * confondre avec la "marge" par article (prix vente - prix achat) utilisee
 * dans l'ecran Marge Caisse - concept different malgre le nom partage.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MargeDetailDTO {

    private RessourceConsolideeDTO ressources;

    @Builder.Default
    private List<Charge> charges = List.of();

    @Builder.Default
    private BigDecimal totalCharges = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal marge = BigDecimal.ZERO;
}
