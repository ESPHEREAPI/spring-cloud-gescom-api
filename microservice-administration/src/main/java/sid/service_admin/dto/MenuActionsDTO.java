package sid.service_admin.dto;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Une ligne de la matrice Profil x Menu x Action pour un profil donne : le
 * menu, son module parent, et l'ensemble des actions (READ/WRITE/UPDATE/
 * DELETE/PRINT) actuellement accordees a ce profil sur ce menu.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuActionsDTO {
    private Long menuId;
    private String menuCode;
    private String menuDescription;
    private Long moduleId;
    private String moduleCode;
    private String moduleDescription;
    private Set<String> actions;
}
