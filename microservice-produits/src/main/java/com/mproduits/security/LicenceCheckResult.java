package com.mproduits.security;

import com.mproduits.dto.LicenceStatutDTO;

public class LicenceCheckResult {

    private final boolean allowed;
    private final String message;
    private final LicenceStatutDTO statut;

    private LicenceCheckResult(boolean allowed, String message, LicenceStatutDTO statut) {
        this.allowed = allowed;
        this.message = message;
        this.statut = statut;
    }

    public static LicenceCheckResult allow(LicenceStatutDTO statut) {
        return new LicenceCheckResult(true, null, statut);
    }

    public static LicenceCheckResult deny(String message) {
        return new LicenceCheckResult(false, message, null);
    }

    public boolean isAllowed() {
        return allowed;
    }

    public String getMessage() {
        return message;
    }

    public LicenceStatutDTO getStatut() {
        return statut;
    }
}
