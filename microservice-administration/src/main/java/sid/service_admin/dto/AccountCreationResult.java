package sid.service_admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resultat de la creation d'un compte admin (systeme ou compagnie).
 * generatedPassword n'est renseigne que si aucun mot de passe n'a ete fourni
 * a la creation - a communiquer une seule fois a l'interesse.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreationResult {
    private UserDTO user;
    private String generatedPassword;
}
