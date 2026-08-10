package com.mproduits.dto;

import com.mproduits.enums.TypePromotion;
import java.util.Date;
import lombok.Data;

@Data
public class PromotionRequest {
    private TypePromotion typePromotion;
    private Date dateDebutPromo;
    private Date dateFinPromo;
}
