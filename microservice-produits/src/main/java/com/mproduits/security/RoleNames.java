package com.mproduits.security;

/**
 * Copie independante des constantes de la hierarchie admin (voir
 * sid.service_admin.security.RoleNames dans microservice-administration) —
 * pas de lib partagee entre modules Maven independants dans ce depot.
 */
public final class RoleNames {

    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";
    public static final String COMPANY_ADMIN = "COMPANY_ADMIN";

    private RoleNames() {
    }
}
