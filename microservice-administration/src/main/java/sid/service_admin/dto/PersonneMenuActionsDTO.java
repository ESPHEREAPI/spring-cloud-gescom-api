package sid.service_admin.dto;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Une ligne de la matrice Menu x Action effective pour un utilisateur precis :
 * les actions heritees de son Profil, celles qu'une exception lui ajoute ou
 * lui retire, et le resultat final (actionsEffectives) - c'est ce dernier
 * ensemble qui determine reellement ce que l'utilisateur peut faire (voir
 * PersonnePermissionService).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonneMenuActionsDTO {
    private Long menuId;
    private String menuCode;
    private String menuDescription;
    private Long moduleId;
    private String moduleCode;
    private String moduleDescription;
    private Set<String> actionsEffectives;
    private Set<String> actionsHeritees;
    private Set<String> actionsExceptionAjoutees;
    private Set<String> actionsExceptionRetirees;
}
