package sid.service_admin.enums;

/**
 * Format de generation automatique du login d'un utilisateur cree pour une
 * compagnie (voir CompagnieParametres#loginFormat et LoginGeneratorService).
 * Ne s'applique qu'aux employes crees via UserService#createUser - les comptes
 * de la hierarchie admin (AdminAccountService) gardent leur propre generation.
 */
public enum LoginFormat {
    /** Ex: "jdupont" (initiale prenom + nom) - format par defaut, historique. */
    INITIALE_NOM,
    /** Ex: "jean.dupont". */
    PRENOM_POINT_NOM,
    /** Ex: "dupontj" (nom + initiale prenom). */
    NOM_INITIALE_PRENOM,
    /** Ex: "LIB-0001" - prefixe derive du type de commerce de la compagnie + sequence. */
    CODE_TYPE_SEQUENCE
}
