package sid.service_admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sid.service_admin.dto.CreateLicenceDTO;
import sid.service_admin.dto.LicenceDTO;
import sid.service_admin.service.LicenceService;

/**
 * Gestion des licences. La generation est ouverte a SYSTEM_ADMIN (pour les
 * compagnies qu'il a lui-meme creees, verifie dans le service) et
 * SUPER_ADMIN (sans restriction) ; suspendre/revoquer/reactiver restent
 * reserves au SUPER_ADMIN (controle en dernier ressort demande).
 */
@RestController
@RequestMapping("/licences")
public class LicenceController {

    private final LicenceService licenceService;

    public LicenceController(LicenceService licenceService) {
        this.licenceService = licenceService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN')")
    public ResponseEntity<LicenceDTO> generer(@RequestBody CreateLicenceDTO dto, Authentication authentication) {
        return ResponseEntity.ok(licenceService.genererLicence(dto, authentication.getName()));
    }

    @GetMapping("/compagnie/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN')")
    public ResponseEntity<LicenceDTO> getByCompagnie(@PathVariable Long id) {
        return ResponseEntity.ok(licenceService.getByCompagnie(id));
    }

    @PostMapping("/compagnie/{id}/revoquer")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<LicenceDTO> revoquer(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(licenceService.revoquer(id, authentication.getName()));
    }

    @PostMapping("/compagnie/{id}/suspendre")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<LicenceDTO> suspendre(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(licenceService.suspendre(id, authentication.getName()));
    }

    @PostMapping("/compagnie/{id}/reactiver")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<LicenceDTO> reactiver(@PathVariable Long id) {
        return ResponseEntity.ok(licenceService.reactiver(id));
    }
}
