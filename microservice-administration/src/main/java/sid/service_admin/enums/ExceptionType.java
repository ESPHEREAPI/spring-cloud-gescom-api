package sid.service_admin.enums;

/**
 * Type d'exception de permission posee sur un utilisateur precis, par-dessus
 * les droits herites de son Profil (voir PersonnePermissionException).
 */
public enum ExceptionType {
    GRANT, REVOKE
}
