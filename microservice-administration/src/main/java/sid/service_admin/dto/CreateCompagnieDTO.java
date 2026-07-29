package sid.service_admin.dto;

import lombok.Data;
import sid.service_admin.enums.TypeCommerce;

@Data
public class CreateCompagnieDTO {
    private String nom;
    private TypeCommerce typeCommerce;
    private String adresse;
    private String tel;
    private String email;

    private String adminUserName;
    private String adminFirstName;
    private String adminLastname;
    private String adminEmail;
    /** Optionnel : si absent, un mot de passe est genere aleatoirement. */
    private String adminPassword;
}
