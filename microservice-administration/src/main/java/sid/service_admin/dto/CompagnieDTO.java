package sid.service_admin.dto;

import java.util.Date;
import lombok.Data;
import sid.service_admin.enums.TypeCommerce;

@Data
public class CompagnieDTO {
    private Long id;
    private String nom;
    private String code;
    private TypeCommerce typeCommerce;
    private Boolean actif;
    private Date dateCreation;
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
    private String adminUserName;
    private String adminNom;
    private String adminEmail;
}
