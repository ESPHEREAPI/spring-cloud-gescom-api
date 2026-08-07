package com.mproduits.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ligne agregee (jamais le detail d'une vente) pour le dashboard
 * administrateur systeme - voir PlatformDashboardController.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VenteParCompagnieDTO {
    private Long compagnieId;
    private String compagnieNom;
    private BigDecimal totalVentes;
    private long nombreTransactions;
}
