package sid.service_admin.dto;

import lombok.Data;
import sid.service_admin.enums.TypeCommerce;

@Data
public class UpdateCompagnieDTO {
    private String nom;
    private TypeCommerce typeCommerce;
    private String adresse;
    private String tel;
    private String email;
    private String capital;
    private String numeroContribuable;
    private String nui;
    private String rccm;
    private String siteWeb;
    private String directeur;
    private String logoChemin;
    private String bp;
    private String quartier;
    private String ville;
}
