package com.mproduits.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mproduits.model.Ressource;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ressources d'une boutique sur une periode, consolidees a l'affichage (pas
 * en base) : les ressources saisies manuellement, PLUS la caisse (ventes
 * terminees) et les versements clients (valides), qui sont des types de
 * ressource "systeme" reflechis automatiquement depuis les modules
 * Vente/Facturation - l'utilisateur n'a jamais a les ressaisir a la main.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RessourceConsolideeDTO {

    @Builder.Default
    private List<Ressource> ressourcesManuelles = List.of();

    @Builder.Default
    private BigDecimal totalRessourcesManuelles = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal totalCaisse = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal totalVersementClient = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;
}
