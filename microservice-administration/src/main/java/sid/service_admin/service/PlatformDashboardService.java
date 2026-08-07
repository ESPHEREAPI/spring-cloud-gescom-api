package sid.service_admin.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sid.service_admin.dto.CompagnieOverviewDTO;
import sid.service_admin.model.Compagnie;
import sid.service_admin.model.Licence;
import sid.service_admin.repository.CompagnieRepository;
import sid.service_admin.repository.LicenceRepository;
import sid.service_admin.repository.PersonneRepository;

/**
 * Vue plateforme reservee aux administrateurs systeme (SUPER_ADMIN/
 * SYSTEM_ADMIN) : supervision de l'ensemble des compagnies (statut, licence,
 * effectif). Volontairement limite a des informations de supervision - pour
 * les donnees de gestion d'une compagnie (ventes, stock...), voir les
 * endpoints agreges dedies de microservice-produits.
 */
@Service
@RequiredArgsConstructor
public class PlatformDashboardService {

    private final CompagnieRepository compagnieRepository;
    private final PersonneRepository personneRepository;
    private final LicenceRepository licenceRepository;

    @Transactional(readOnly = true)
    public List<CompagnieOverviewDTO> getCompagniesOverview() {
        List<Compagnie> compagnies = compagnieRepository.findAll();
        return compagnies.stream().map(this::toOverview).collect(Collectors.toList());
    }

    private CompagnieOverviewDTO toOverview(Compagnie compagnie) {
        long nombreUtilisateurs = personneRepository.countByCompagnie_Id(compagnie.getId());
        Licence licence = licenceRepository.findFirstByCompagnie_IdOrderByCreatedAtDesc(compagnie.getId())
                .orElse(null);

        return new CompagnieOverviewDTO(
                compagnie.getId(),
                compagnie.getNom(),
                compagnie.getTypeCommerce() != null ? compagnie.getTypeCommerce().name() : null,
                compagnie.getActif(),
                compagnie.getDateCreation(),
                nombreUtilisateurs,
                licence != null ? licence.getStatut().name() : "AUCUNE",
                licence != null ? licence.getDateExpiration() : null);
    }
}
