package sid.service_admin.security;

/**
 * Noms des roles de la hierarchie multi-compagnies (independants du systeme
 * de roles metier existant ADMIN/CAISSIER/COMMERCIAL/...).
 */
public final class RoleNames {

    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";
    public static final String COMPANY_ADMIN = "COMPANY_ADMIN";

    /**
     * Role unique assigne automatiquement a tout utilisateur compagnie cree
     * via le formulaire standard (UserService#createUser) - les droits reels
     * viennent exclusivement du Profil assigne (voir ProfilPermissionMatrixService),
     * pas du role. Un client ne peut jamais choisir/changer ce role.
     */
    public static final String EMPLOYE = "EMPLOYE";

    private RoleNames() {
    }
}
