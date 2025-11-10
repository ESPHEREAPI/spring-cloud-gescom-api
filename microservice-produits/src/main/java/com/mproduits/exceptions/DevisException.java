package com.mproduits.exceptions;

/**
 * Exception métier pour la gestion des devis
 * 
 * Lancée lorsqu'une opération sur un devis ne peut pas être effectuée:
 * - Devis non trouvé
 * - Modification impossible (statut incorrect)
 * - Validation échouée
 * - Stock insuffisant
 * - Devis expiré
 * 
 * @author USER01
 * @version 2.0
 */
public class DevisException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Code d'erreur (optionnel)
     */
    private String errorCode;

    /**
     * Données supplémentaires (optionnel)
     */
    private Object additionalData;

    /**
     * Constructeur simple avec message
     */
    public DevisException(String message) {
        super(message);
    }

    /**
     * Constructeur avec message et cause
     */
    public DevisException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructeur avec message et code d'erreur
     */
    public DevisException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructeur complet
     */
    public DevisException(String message, String errorCode, Object additionalData) {
        super(message);
        this.errorCode = errorCode;
        this.additionalData = additionalData;
    }

    // Getters
    public String getErrorCode() {
        return errorCode;
    }

    public Object getAdditionalData() {
        return additionalData;
    }

    // ========== MÉTHODES FACTORY POUR EXCEPTIONS COURANTES ==========

    /**
     * Devis non trouvé
     */
    public static DevisException notFound(Long devisId) {
        return new DevisException(
                String.format("Devis non trouvé: %d", devisId),
                "DEVIS_NOT_FOUND"
        );
    }

    /**
     * Devis non modifiable
     */
    public static DevisException notModifiable(String statut) {
        return new DevisException(
                String.format("Impossible de modifier le devis. Statut actuel: %s", statut),
                "DEVIS_NOT_MODIFIABLE"
        );
    }

    /**
     * Devis expiré
     */
    public static DevisException expired(String numeroDevis) {
        return new DevisException(
                String.format("Le devis %s est expiré", numeroDevis),
                "DEVIS_EXPIRED"
        );
    }

    /**
     * Stock insuffisant
     */
    public static DevisException insufficientStock(String details) {
        return new DevisException(
                "Stock insuffisant: " + details,
                "STOCK_INSUFFICIENT"
        );
    }

    /**
     * Client non trouvé
     */
    public static DevisException clientNotFound(Long clientId) {
        return new DevisException(
                String.format("Client non trouvé: %d", clientId),
                "CLIENT_NOT_FOUND"
        );
    }

    /**
     * Produit non trouvé
     */
    public static DevisException produitNotFound(Long produitId) {
        return new DevisException(
                String.format("Produit non trouvé: %d", produitId),
                "PRODUIT_NOT_FOUND"
        );
    }

    /**
     * Validation échouée
     */
    public static DevisException validationFailed(String message) {
        return new DevisException(
                "Erreur de validation: " + message,
                "VALIDATION_FAILED"
        );
    }

    /**
     * Statut invalide
     */
    public static DevisException invalidStatus(String statutActuel, String statutAttendu) {
        return new DevisException(
                String.format("Statut invalide. Attendu: %s, Actuel: %s", 
                        statutAttendu, statutActuel),
                "INVALID_STATUS"
        );
    }

    /**
     * Opération non autorisée
     */
    public static DevisException operationNotAllowed(String operation, String reason) {
        return new DevisException(
                String.format("Opération '%s' non autorisée: %s", operation, reason),
                "OPERATION_NOT_ALLOWED"
        );
    }
}