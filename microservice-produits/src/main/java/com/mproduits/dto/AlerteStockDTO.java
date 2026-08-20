package com.mproduits.dto;

import java.math.BigDecimal;

/**
 * Projection scalaire pour l'alerte stock faible (voir ServiceCommande.alertstock,
 * CommandeController /alert-stock) - aucune entite Produit hydratee. Charger
 * les PrixArticles en entite complete pour ce dashboard (execute a chaque
 * connexion) chargeait AUSSI, via l'association @ManyToOne eager
 * PointVente.produit, des dizaines/centaines de Produit dans la session
 * Hibernate partagee de la requete (Open Session In View) sur un catalogue
 * volumineux - meme cause que "Found shared references to a collection" deja
 * rencontree et corrigee ailleurs (StockRestaurationService, DevisService).
 */
public record AlerteStockDTO(Long produitId, String libelle, String reference,
        BigDecimal stockFinalTheorie, String categorieLibelle) {
}
