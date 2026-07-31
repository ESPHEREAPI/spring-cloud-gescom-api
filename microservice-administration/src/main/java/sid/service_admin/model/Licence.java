package sid.service_admin.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;
import sid.service_admin.enums.LicenceStatut;

/**
 * Une ligne = un cycle de vie de licence pour une compagnie (generation ->
 * activation -> eventuelle suspension/revocation). Une compagnie accumule un
 * historique de lignes au fil des generations/renouvellements - la ligne
 * "courante" depend du contexte : la plus recente ACTIVE pour l'application
 * (voir LicenceService.getCurrentActive, utilise par microservice-produits),
 * la plus recente tout statut confondu pour la gestion SUPER_ADMIN/SYSTEM_ADMIN
 * (voir LicenceService.getLatest).
 *
 * Les anciennes colonnes (LicenseType, duree, licenseNumber, adresseMac,
 * version) issues du stub original sont orphelines — ddl-auto=update ne les
 * supprime pas, nettoyage manuel a prevoir cote DBA, non bloquant.
 */
@Entity
@Table(name = "licence")
@Data
@NoArgsConstructor
public class Licence implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "compagnie_id", nullable = false)
    private Compagnie compagnie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LicenceStatut statut;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_debut")
    private Date dateDebut;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_expiration")
    private Date dateExpiration;

    @Column(name = "max_utilisateurs")
    private Integer maxUtilisateurs;

    @Column(name = "max_boutiques")
    private Integer maxBoutiques;

    @ElementCollection
    @CollectionTable(name = "licence_module", joinColumns = @JoinColumn(name = "licence_id"))
    @Column(name = "module")
    private Set<String> modulesActifs = new HashSet<>();

    @Column(length = 2000)
    private String cle;

    /** Licence d'essai auto-service (voir LicenceSettings) - une seule autorisee par compagnie. */
    @Column(nullable = false)
    private Boolean essai = Boolean.FALSE;

    @Column(name = "created_by")
    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "revoked_by")
    private String revokedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_revocation")
    private Date dateRevocation;
}
