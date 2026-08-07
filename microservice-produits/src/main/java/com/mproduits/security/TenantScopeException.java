package com.mproduits.security;

/**
 * Levee par BoutiqueAccessGuard quand une boutique demandee n'appartient pas
 * a la compagnie de l'utilisateur courant. Traduite en HTTP 403 par
 * GlobalExceptionHandler (voir com.mproduits.exceptions).
 */
public class TenantScopeException extends RuntimeException {

    public TenantScopeException(String message) {
        super(message);
    }
}
