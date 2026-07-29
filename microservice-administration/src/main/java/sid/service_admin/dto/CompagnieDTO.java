package sid.service_admin.dto;

import java.util.Date;
import lombok.Data;
import sid.service_admin.enums.TypeCommerce;

@Data
public class CompagnieDTO {
    private Long id;
    private String nom;
    private TypeCommerce typeCommerce;
    private Boolean actif;
    private Date dateCreation;
    private String adresse;
    private String tel;
    private String email;
}
