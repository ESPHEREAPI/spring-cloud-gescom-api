package com.mproduits.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Une ligne de l'apercu d'un fichier de restauration de stock (voir
 * StockRestaurationService.previsualiserImport) - rien n'est encore ecrit en
 * base a ce stade. erreur non-null = ligne bloquante (reference/boutique
 * introuvable, quantite invalide).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LigneApercuImportStockDTO {
    private int ligneNo;
    private String reference;
    private String boutiqueNom;
    private BigDecimal ancienneQuantite;
    private BigDecimal nouvelleQuantite;
    private String erreur;
    // true si ce produit n'existe pas encore : l'application creera le
    // produit (et son point de vente) au lieu de mettre a jour un stock existant.
    private boolean nouveauProduit;
    // true si ce produit existe deja au catalogue mais n'a encore aucun
    // point de vente dans cette boutique precise (catalogue partage entre
    // boutiques) - un nouveau point de vente sera cree, sans recreer le
    // produit. Toujours vrai quand nouveauProduit l'est aussi.
    private boolean nouveauPointVente;
    // true si le fichier n'a pas de prix de vente exploitable pour cette
    // ligne (colonne absente du format configure, ou cellule vide/illisible) -
    // PAS bloquant (voir StockRestaurationService.lireNombreOuZero, le prix
    // reste volontairement facultatif a l'import) mais affiche pour eviter la
    // surprise silencieuse d'un produit importe a 0 FCFA.
    private boolean prixManquant;
}
