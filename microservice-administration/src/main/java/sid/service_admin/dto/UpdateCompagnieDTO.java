package sid.service_admin.dto;

import lombok.Data;
import sid.service_admin.enums.TypeCommerce;

@Data
public class UpdateCompagnieDTO {
    private String nom;
    private TypeCommerce typeCommerce;
    private Boolean actif;
    private String adresse;
    private String tel;
    private String email;
}
