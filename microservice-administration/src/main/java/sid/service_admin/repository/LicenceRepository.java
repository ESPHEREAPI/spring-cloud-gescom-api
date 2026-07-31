package sid.service_admin.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sid.service_admin.enums.LicenceStatut;
import sid.service_admin.model.Licence;

public interface LicenceRepository extends JpaRepository<Licence, Long> {

    /** La plus recente avec ce statut precis (ex: ACTIVE pour l'application, GENEREE pour l'activation en attente). */
    Optional<Licence> findFirstByCompagnie_IdAndStatutOrderByCreatedAtDesc(Long compagnieId, LicenceStatut statut);

    /** La plus recente tout statut confondu - usage gestion SUPER_ADMIN/SYSTEM_ADMIN. */
    Optional<Licence> findFirstByCompagnie_IdOrderByCreatedAtDesc(Long compagnieId);

    /** Historique complet, plus recent en premier. */
    List<Licence> findAllByCompagnie_IdOrderByCreatedAtDesc(Long compagnieId);

    boolean existsByCompagnie_IdAndEssaiTrue(Long compagnieId);
}
