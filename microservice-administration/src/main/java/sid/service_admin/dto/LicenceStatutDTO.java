package sid.service_admin.dto;

import java.util.Date;
import java.util.Set;
import lombok.Data;

/**
 * Reponse de l'endpoint interne /internal/licences/compagnie/{id}/statut,
 * consommee par microservice-produits (LicenceClient) pour appliquer la
 * licence. Volontairement minimale (pas de cle, pas de qui-a-cree) : cet
 * endpoint n'est pas destine a des clients publics.
 */
@Data
public class LicenceStatutDTO {
    private String statut;
    private Date dateExpiration;
    private Integer maxUtilisateurs;
    private Integer maxBoutiques;
    private Set<String> modules;
}
