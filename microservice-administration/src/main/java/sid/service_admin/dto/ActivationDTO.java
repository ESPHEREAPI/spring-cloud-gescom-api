package sid.service_admin.dto;

import lombok.Data;

/** Corps de requete pour activer/desactiver un compte ou une compagnie - le motif est obligatoire (trace d'audit). */
@Data
public class ActivationDTO {
    private String motif;
}
