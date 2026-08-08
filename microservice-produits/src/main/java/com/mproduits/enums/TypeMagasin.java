package com.mproduits.enums;

/**
 * Type explicite d'un Magasin, remplace la deduction implicite via la
 * nullite de boutique (fragile - rien n'empechait un etat incoherent en
 * base et chaque ecran devait reimplementer le test null/non-null).
 */
public enum TypeMagasin {
    /** Rattache a une Boutique - la reception y est immediatement disponible a la vente. */
    POINT_DE_VENTE,
    /** Sans Boutique - stock centralise, distribue plus tard par transfert vers une ou plusieurs boutiques. */
    DEPOT_CENTRAL
}
