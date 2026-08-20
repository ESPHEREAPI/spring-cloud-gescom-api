package com.mproduits.dto;

import java.math.BigDecimal;

/**
 * Projection scalaire pour l'ecran "Gestion des Points de Vente"
 * (/prix-articles/all/{boutiqueid}) - remplace un List&lt;PrixArticles&gt;
 * d'entites completes qui, sur un catalogue volumineux, declenchait "Found
 * shared references to a collection" (meme cause racine que
 * AlerteStockDTO/StockRestaurationService/DevisService : PointVente.produit
 * est charge EAGER, et chaque Produit porte des collections cascade=ALL).
 */
public record PrixArticlesAdminDTO(Long id, BigDecimal prixVenteNet, BigDecimal tva, BigDecimal remise,
        PointVenteAdminDTO pointVente) {
}
