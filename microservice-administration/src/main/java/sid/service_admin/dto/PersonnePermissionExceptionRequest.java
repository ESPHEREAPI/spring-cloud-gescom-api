package sid.service_admin.dto;

import lombok.Data;

/**
 * Pose une exception (GRANT/REVOKE) pour un utilisateur precis sur un couple
 * (menu, action) - voir PersonnePermissionService.definirException.
 */
@Data
public class PersonnePermissionExceptionRequest {
    private Long menuId;
    private String action;
    private String type;
}
