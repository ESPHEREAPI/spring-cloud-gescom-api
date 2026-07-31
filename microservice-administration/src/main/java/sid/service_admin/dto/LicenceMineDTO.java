package sid.service_admin.dto;

import lombok.Data;

/** Vue self-service COMPANY_ADMIN de l'etat de licence de sa propre compagnie. */
@Data
public class LicenceMineDTO {
    private LicenceDTO active;
    private LicenceDTO pending;
    private boolean essaiDisponible;
}
