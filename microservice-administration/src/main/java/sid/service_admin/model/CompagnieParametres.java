package sid.service_admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import sid.service_admin.enums.LoginFormat;

/**
 * Parametrage "Option Entreprise" d'une compagnie : securite/connexion,
 * fiscalite par defaut, format du ticket de caisse et de la facture A4. Une
 * seule ligne par compagnie (voir CompagnieParametresService#getOrCreate).
 * Ces valeurs sont un point de configuration - leur exploitation reelle
 * (application de la politique de mot de passe, lecture par
 * microservice-produits pour generer tickets/factures) est une etape
 * ulterieure, hors perimetre de cette premiere version.
 */
@Entity
@Table(name = "compagnie_parametres")
@Data
@NoArgsConstructor
public class CompagnieParametres implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "compagnie_id", nullable = false, unique = true)
    private Compagnie compagnie;

    // --- Securite / connexion ---
    @Column(name = "pwd_longueur_min")
    private Integer pwdLongueurMin = 8;

    @Column(name = "pwd_majuscule_requise")
    private Boolean pwdMajusculeRequise = Boolean.TRUE;

    @Column(name = "pwd_chiffre_requis")
    private Boolean pwdChiffreRequis = Boolean.TRUE;

    @Column(name = "pwd_special_requis")
    private Boolean pwdSpecialRequis = Boolean.FALSE;

    /** 0 = pas d'expiration. */
    @Column(name = "pwd_expiration_jours")
    private Integer pwdExpirationJours = 0;

    @Column(name = "tentatives_max_avant_verrouillage")
    private Integer tentativesMaxAvantVerrouillage = 5;

    @Column(name = "duree_verrouillage_minutes")
    private Integer dureeVerrouillageMinutes = 30;

    @Column(name = "duree_session_minutes")
    private Integer dureeSessionMinutes = 60;

    /**
     * Format d'auto-generation du login des employes crees pour cette
     * compagnie (voir LoginGeneratorService) - CODE_TYPE_SEQUENCE derive son
     * prefixe du type de commerce (LIB/QUI/MINI/SUP/BTQ/GEN), utilisable quel
     * que soit le type de compagnie geree par la plateforme.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "login_format")
    private LoginFormat loginFormat = LoginFormat.INITIALE_NOM;

    // --- Fiscalite par defaut ---
    @Column(name = "regime_fiscal")
    private String regimeFiscal;

    @Column(name = "tva_applicable")
    private Boolean tvaApplicable = Boolean.TRUE;

    @Column(name = "taux_tva_defaut")
    private BigDecimal tauxTvaDefaut;

    /** Les prix saisis/affiches (articles, ticket) s'entendent TVA incluse. */
    @Column(name = "prix_ttc")
    private Boolean prixTTC = Boolean.TRUE;

    // --- Ticket de caisse ---
    @Column(name = "ticket_largeur_mm")
    private Integer ticketLargeurMm = 80;

    @Column(name = "ticket_afficher_logo")
    private Boolean ticketAfficherLogo = Boolean.TRUE;

    @Column(name = "ticket_en_tete", length = 500)
    private String ticketEnTete;

    @Column(name = "ticket_pied_de_page", length = 500)
    private String ticketPiedDePage;

    /** NUI/RCCM (voir Compagnie) affiches sur le ticket - mention souvent exigee legalement. */
    @Column(name = "ticket_afficher_mentions_fiscales")
    private Boolean ticketAfficherMentionsFiscales = Boolean.FALSE;

    // --- Facture A4 ---
    @Column(name = "facture_devise")
    private String factureDevise = "XAF";

    @Column(name = "facture_afficher_logo")
    private Boolean factureAfficherLogo = Boolean.TRUE;

    @Column(name = "facture_mentions_legales", length = 1000)
    private String factureMentionsLegales;

    /** Prefixe de numerotation des factures, ex "FA-2026-". */
    @Column(name = "facture_prefixe_numerotation")
    private String facturePrefixeNumerotation;

    @Column(name = "facture_echeance_jours_defaut")
    private Integer factureEcheanceJoursDefaut = 30;

    // --- Bon d'achat ---
    /**
     * Duree de validite par defaut (en jours) d'un bon d'achat emis par un
     * caissier pour compenser un reliquat de monnaie (pas de piece/billet
     * disponible pour rendre la monnaie exacte). Lu par microservice-produits
     * (BonAchatService.emettreDepuisRendu) via un miroir en lecture seule de
     * cette table, meme pattern que Compagnie.
     */
    @Column(name = "bon_achat_duree_validite_jours")
    private Integer bonAchatDureeValiditeJours = 90;

    public CompagnieParametres(Compagnie compagnie) {
        this.compagnie = compagnie;
    }
}
