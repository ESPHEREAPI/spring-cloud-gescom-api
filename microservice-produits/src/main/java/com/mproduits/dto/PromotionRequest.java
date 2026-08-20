package com.mproduits.dto;

import com.mproduits.enums.TypePromotion;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class PromotionRequest {
    private TypePromotion typePromotion;
    private Date dateDebutPromo;
    private Date dateFinPromo;
    // Nullable - non fournis = prix/remise inchanges (endpoint aussi utilise
    // pour ne modifier que le type/les dates de promotion, voir
    // CommandeController.definirPromotion).
    private BigDecimal prixVenteNet;
    private BigDecimal remise;
}
