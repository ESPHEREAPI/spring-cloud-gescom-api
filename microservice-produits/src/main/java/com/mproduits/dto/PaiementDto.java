package com.mproduits.dto;

import java.math.BigDecimal;
import lombok.Data;

/**
 * Une ligne de paiement au sein d'une vente a paiement mixte
 * (ex: espece + Orange Money, ou espece + bon d'achat).
 */
@Data
public class PaiementDto {

    private String typePaiement;
    private BigDecimal montant;
    private String reference;
}
