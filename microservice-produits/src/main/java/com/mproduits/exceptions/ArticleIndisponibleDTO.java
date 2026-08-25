package com.mproduits.exceptions;

/**
 * Une ligne de commande publique (storefront) que le serveur ne peut pas
 * honorer (produit introuvable dans la boutique, ou stock insuffisant) -
 * portee par ArticlesIndisponiblesException pour que le frontend puisse
 * identifier et surligner precisement la ou les lignes du panier a retirer,
 * plutot que de decouvrir les problemes un par un a chaque nouvelle tentative.
 */
public record ArticleIndisponibleDTO(Long produitId, String motif) {
}
