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
import jakarta.persistence.OneToOne;
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
 * Licence d'une compagnie : une licence active par compagnie. Redesign
 * complet de l'ancien stub (jamais cable, pensait un modele hors-ligne par
 * adresse MAC) pour un modele en ligne, par compagnie, avec revocation
 * quasi instantanee (voir LicenceService/LicenceStatusService cote produits).
 *
 * Les anciennes colonnes (LicenseType, duree, licenseNumber, adresseMac,
 * version) deviennent orphelines apres ce changement — ddl-auto=update ne
 * les supprime pas, nettoyage manuel a prevoir cote DBA, non bloquant.
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

    @OneToOne
    @JoinColumn(name = "compagnie_id", nullable = false, unique = true)
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
