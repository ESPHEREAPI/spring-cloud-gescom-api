package sid.service_admin.dto;

import lombok.Data;

/**
 * Bascule une case de la matrice Profil x Menu x Action (accorde si absente,
 * retire si presente). `action` est le code de l'Action (catalogue
 * dynamique, voir Action.java) - plus l'ancien enum Java OperationType.
 */
@Data
public class TogglePermissionRequest {
    private Long profilId;
    private Long menuId;
    private String action;
    private boolean granted;
}
