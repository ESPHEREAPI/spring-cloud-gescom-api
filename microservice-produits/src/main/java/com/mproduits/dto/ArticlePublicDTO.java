package com.mproduits.dto;

import com.mproduits.enums.TypePromotion;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class ArticlePublicDTO {
    private Long prixArticlesId;
    private Long produitId;
    private String reference;
    private String libelle;
    private String description;
    private String photoName;
    private Long categorieId;
    private String categorieLibelle;
    private BigDecimal prixVenteNet;
    private BigDecimal prixVenteTTC;
    private BigDecimal remise;
    private BigDecimal quantiteDisponible;
    private TypePromotion typePromotion;
    private Date dateDebutPromo;
    private Date dateFinPromo;
    /** Calcule cote serveur : promotion en cours compte tenu des bornes de dates - la promo expire seule sans intervention. */
    private boolean promotionActive;
}
