package sid.service_admin.security;

/**
 * Noms des roles de la hierarchie multi-compagnies (independants du systeme
 * de roles metier existant ADMIN/CAISSIER/COMMERCIAL/...).
 */
public final class RoleNames {

    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";
    public static final String COMPANY_ADMIN = "COMPANY_ADMIN";

    private RoleNames() {
    }
}
