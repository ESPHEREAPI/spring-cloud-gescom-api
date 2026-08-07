package sid.service_admin.dto;

import lombok.Data;
import sid.service_admin.enums.OperationType;

/**
 * Bascule une case de la matrice Profil x Menu x Action (accorde si absente,
 * retire si presente).
 */
@Data
public class TogglePermissionRequest {
    private Long profilId;
    private Long menuId;
    private OperationType action;
    private boolean granted;
}
