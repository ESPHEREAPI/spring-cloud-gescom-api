package sid.service_admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import sid.service_admin.dto.ActiverLicenceDTO;
import sid.service_admin.dto.CreateLicenceDTO;
import sid.service_admin.dto.LicenceDTO;
import sid.service_admin.dto.LicenceMineDTO;
import sid.service_admin.dto.LicenceSettingsDTO;
import sid.service_admin.service.LicenceService;
import sid.service_admin.service.LicenceSettingsService;

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
    private final LicenceSettingsService licenceSettingsService;

    public LicenceController(LicenceService licenceService, LicenceSettingsService licenceSettingsService) {
        this.licenceService = licenceService;
        this.licenceSettingsService = licenceSettingsService;
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

    /** Catalogue de modules applicable au type de commerce de la compagnie - alimente le formulaire de licence. */
    @GetMapping("/compagnie/{id}/modules-disponibles")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN')")
    public ResponseEntity<List<String>> getModulesDisponibles(@PathVariable Long id) {
        return ResponseEntity.ok(licenceService.getModulesDisponibles(id));
    }

    /** Catalogue complet (non filtre par type de commerce) - utilise par l'ecran des parametres de l'essai gratuit. */
    @GetMapping("/modules-catalogue")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<String>> getModulesCatalogue() {
        return ResponseEntity.ok(java.util.Arrays.stream(sid.service_admin.enums.ModuleLicence.values())
                .map(sid.service_admin.enums.ModuleLicence::getCode)
                .sorted()
                .toList());
    }

    /**
     * Modules actifs de la licence de la compagnie de l'appelant, pour le gating du sidebar.
     * Ouvert a tout utilisateur authentifie (pas seulement COMPANY_ADMIN) - contrairement a /me,
     * ne renvoie que des codes de modules, aucune autre donnee de licence (cle, dates, quotas).
     */
    @GetMapping("/mes-modules-actifs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<java.util.Set<String>> getMesModulesActifs(Authentication authentication) {
        return ResponseEntity.ok(licenceService.getModulesActifsPourUtilisateurCourant(authentication.getName()));
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

    // ===================== Self-service COMPANY_ADMIN =====================

    @GetMapping("/me")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<LicenceMineDTO> getMine(Authentication authentication) {
        return ResponseEntity.ok(licenceService.getMine(authentication.getName()));
    }

    @GetMapping("/me/historique")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<List<LicenceDTO>> getHistorique(Authentication authentication) {
        return ResponseEntity.ok(licenceService.getHistorique(authentication.getName()));
    }

    @PostMapping("/me/activer")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<LicenceDTO> activer(@RequestBody ActiverLicenceDTO dto, Authentication authentication) {
        return ResponseEntity.ok(licenceService.activerParCle(authentication.getName(), dto));
    }

    @PostMapping("/me/essai")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<LicenceDTO> demarrerEssai(Authentication authentication) {
        return ResponseEntity.ok(licenceService.demarrerEssai(authentication.getName()));
    }

    // ===================== Parametres essai (SUPER_ADMIN uniquement) =====================

    @GetMapping("/parametres")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<LicenceSettingsDTO> getParametres() {
        return ResponseEntity.ok(licenceSettingsService.get());
    }

    @PutMapping("/parametres")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<LicenceSettingsDTO> updateParametres(@RequestBody LicenceSettingsDTO dto) {
        return ResponseEntity.ok(licenceSettingsService.update(dto));
    }
}
