package sid.service_admin.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Vue plateforme d'une compagnie pour le dashboard administrateur systeme :
 * uniquement des informations de supervision (statut, licence, effectif),
 * jamais de donnees de gestion (ventes, stock, clients...).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompagnieOverviewDTO {
    private Long compagnieId;
    private String nom;
    private String typeCommerce;
    private Boolean actif;
    private Date dateCreation;
    private long nombreUtilisateurs;
    private String statutLicence;
    private Date dateExpirationLicence;
}
