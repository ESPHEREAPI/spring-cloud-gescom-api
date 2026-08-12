package com.mproduits.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Solde consolide d'un client (recouvrement) : total facture, total paye,
 * reste a payer, toutes factures confondues. Utilise pour l'ecran Compte
 * Client (solde d'un ou plusieurs clients) et son classement compagnie
 * ("clients a haute redevance" = plus gros reste-a-payer).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientSoldeDTO {

    private Long clientId;
    private String nom;
    private String code;
    private String telephone;

    @Builder.Default
    private BigDecimal totalFacture = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal totalPaye = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal soldeRestant = BigDecimal.ZERO;

    @Builder.Default
    private long nombreFactures = 0;
}
