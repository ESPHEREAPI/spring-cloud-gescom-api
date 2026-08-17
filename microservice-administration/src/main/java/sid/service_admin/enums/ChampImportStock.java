package sid.service_admin.enums;

/**
 * Catalogue des champs qu'une compagnie peut inclure dans son format de
 * fichier de restauration de stock (voir StockImportFormat). REFERENCE et
 * QUANTITE sont fonctionnellement obligatoires (utilises pour retrouver le
 * produit et calculer la nouvelle quantite) - les autres sont purement
 * informatifs, la ils aident l'administrateur a reconnaitre visuellement
 * une ligne de son fichier.
 */
public enum ChampImportStock {
    REFERENCE, PRODUIT, CATEGORIE, PRIX_VENTE, PRIX_ACHAT, BOUTIQUE, QUANTITE
}
