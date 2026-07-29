package sid.service_admin.dto;

import lombok.Data;

@Data
public class CreateCompagnieResultDTO {
    private CompagnieDTO compagnie;
    private UserDTO admin;
    /** Renseigne uniquement si aucun mot de passe n'a ete fourni pour l'admin compagnie. */
    private String generatedAdminPassword;
}
