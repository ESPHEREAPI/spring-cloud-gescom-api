package sid.service_admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sid.service_admin.dto.LicenceStatutDTO;
import sid.service_admin.service.LicenceService;

/**
 * Appele par microservice-produits (pas par un utilisateur) pour verifier la
 * licence d'une compagnie. Protege par InternalServiceAuthFilter (en-tete
 * X-Internal-Service-Key), pas par un JWT utilisateur.
 */
@RestController
@RequestMapping("/internal/licences")
public class LicenceInternalController {

    private final LicenceService licenceService;

    public LicenceInternalController(LicenceService licenceService) {
        this.licenceService = licenceService;
    }

    @GetMapping("/compagnie/{id}/statut")
    @PreAuthorize("hasRole('INTERNAL_SERVICE')")
    public ResponseEntity<LicenceStatutDTO> statut(@PathVariable Long id) {
        return ResponseEntity.ok(licenceService.getStatutInterne(id));
    }
}
