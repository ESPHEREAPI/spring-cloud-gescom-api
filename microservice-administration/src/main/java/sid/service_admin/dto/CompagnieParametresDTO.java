package sid.service_admin.dto;

import java.math.BigDecimal;
import lombok.Data;
import sid.service_admin.enums.LoginFormat;

@Data
public class CompagnieParametresDTO {

    private Integer pwdLongueurMin;
    private Boolean pwdMajusculeRequise;
    private Boolean pwdChiffreRequis;
    private Boolean pwdSpecialRequis;
    private Integer pwdExpirationJours;
    private Integer tentativesMaxAvantVerrouillage;
    private Integer dureeVerrouillageMinutes;
    private Integer dureeSessionMinutes;
    private LoginFormat loginFormat;

    private String regimeFiscal;
    private Boolean tvaApplicable;
    private BigDecimal tauxTvaDefaut;
    private Boolean prixTTC;

    private Integer ticketLargeurMm;
    private Boolean ticketAfficherLogo;
    private String ticketEnTete;
    private String ticketPiedDePage;
    private Boolean ticketAfficherMentionsFiscales;

    private String factureDevise;
    private Boolean factureAfficherLogo;
    private String factureMentionsLegales;
    private String facturePrefixeNumerotation;
    private Integer factureEcheanceJoursDefaut;
}
