package sid.service_admin.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Parametres de l'essai gratuit en libre-service, configures par le
 * SUPER_ADMIN (voir LicenceController#getParametresEssai). Ligne singleton,
 * id fixe = 1 (creee a la premiere lecture si absente).
 */
@Entity
@Table(name = "licence_settings")
@Data
@NoArgsConstructor
public class LicenceSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id = 1L;

    /** Le bouton "essai gratuit" n'est visible cote compagnie que si ce champ est vrai. */
    @Column(name = "essai_actif", nullable = false)
    private Boolean essaiActif = Boolean.FALSE;

    @Column(name = "essai_duree_jours", nullable = false)
    private Integer essaiDureeJours = 30;

    @Column(name = "essai_max_utilisateurs", nullable = false)
    private Integer essaiMaxUtilisateurs = 2;

    @Column(name = "essai_max_boutiques", nullable = false)
    private Integer essaiMaxBoutiques = 1;

    @ElementCollection
    @CollectionTable(name = "licence_settings_module", joinColumns = @JoinColumn(name = "licence_settings_id"))
    @Column(name = "module")
    private Set<String> essaiModules = new HashSet<>();
}
