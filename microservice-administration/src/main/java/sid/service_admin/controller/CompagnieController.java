package sid.service_admin.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sid.service_admin.dto.ActivationDTO;
import sid.service_admin.dto.CompagnieDTO;
import sid.service_admin.dto.CompagnieParametresDTO;
import sid.service_admin.dto.CreateCompagnieDTO;
import sid.service_admin.dto.CreateCompagnieResultDTO;
import sid.service_admin.dto.ResetPasswordResultDTO;
import sid.service_admin.dto.StockImportFormatDTO;
import sid.service_admin.dto.UpdateCompagnieDTO;
import sid.service_admin.service.CompagnieParametresService;
import sid.service_admin.service.CompagnieService;
import sid.service_admin.service.StockImportFormatService;

/**
 * Creation/gestion des compagnies - reserve aux administrateurs
 * systeme/super-administrateur, sauf /me qui est reserve a l'admin compagnie.
 */
@RestController
@RequestMapping("/compagnies")
public class CompagnieController {

    private final CompagnieService compagnieService;
    private final CompagnieParametresService compagnieParametresService;
    private final StockImportFormatService stockImportFormatService;

    public CompagnieController(CompagnieService compagnieService, CompagnieParametresService compagnieParametresService,
            StockImportFormatService stockImportFormatService) {
        this.compagnieService = compagnieService;
        this.compagnieParametresService = compagnieParametresService;
        this.stockImportFormatService = stockImportFormatService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN')")
    public ResponseEntity<CreateCompagnieResultDTO> create(@RequestBody CreateCompagnieDTO dto, Authentication authentication) {
        return ResponseEntity.ok(compagnieService.createCompagnieWithAdmin(dto, authentication.getName()));
    }

    /**
     * Inscription autonome (public, sans authentification) : un futur
     * administrateur cree lui-meme sa compagnie - voir
     * CompagnieService.inscriptionAutonome. Reste bloque a la connexion
     * (isActive=false) jusqu'a validation du lien recu par email.
     */
    @PostMapping("/self-service")
    public ResponseEntity<Void> inscriptionAutonome(@RequestBody CreateCompagnieDTO dto, HttpServletRequest request) {
        compagnieService.inscriptionAutonome(dto, adresseIpClient(request));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/self-service/verify")
    public ResponseEntity<Void> verifierEmail(@RequestParam String token) {
        compagnieService.verifierEmail(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/self-service/renvoyer")
    public ResponseEntity<Void> renvoyerVerification(@RequestBody Map<String, String> body) {
        compagnieService.renvoyerVerification(body.get("email"));
        return ResponseEntity.ok().build();
    }

    /** Derriere la nginx hote (voir CLAUDE.md), l'adresse reelle du client est dans X-Forwarded-For, pas getRemoteAddr(). */
    private String adresseIpClient(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN')")
    public ResponseEntity<List<CompagnieDTO>> listAll() {
        return ResponseEntity.ok(compagnieService.listAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN')")
    public ResponseEntity<CompagnieDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(compagnieService.getById(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<CompagnieDTO> getOwn(Authentication authentication) {
        return ResponseEntity.ok(compagnieService.getOwn(authentication.getName()));
    }

    /** Libre-service : l'admin compagnie complete/corrige les infos que l'admin systeme ne connaissait pas a la creation. */
    @PutMapping("/me")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<CompagnieDTO> updateOwn(@RequestBody UpdateCompagnieDTO dto, Authentication authentication) {
        return ResponseEntity.ok(compagnieService.updateOwn(authentication.getName(), dto));
    }

    /** "Option Entreprise" : securite/connexion, fiscalite par defaut, format ticket/facture - voir CompagnieParametres. */
    @GetMapping("/me/parametres")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<CompagnieParametresDTO> getOwnParametres(Authentication authentication) {
        return ResponseEntity.ok(compagnieParametresService.getOwn(authentication.getName()));
    }

    @PutMapping("/me/parametres")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<CompagnieParametresDTO> updateOwnParametres(@RequestBody CompagnieParametresDTO dto, Authentication authentication) {
        return ResponseEntity.ok(compagnieParametresService.updateOwn(authentication.getName(), dto));
    }

    /** Format du fichier de restauration de stock (voir StockImportFormat) - personnalise par chaque compagnie. */
    @GetMapping("/me/stock-import-format")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<StockImportFormatDTO> getOwnStockImportFormat(Authentication authentication) {
        return ResponseEntity.ok(stockImportFormatService.getOwn(authentication.getName()));
    }

    @PutMapping("/me/stock-import-format")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<StockImportFormatDTO> updateOwnStockImportFormat(@RequestBody StockImportFormatDTO dto, Authentication authentication) {
        return ResponseEntity.ok(stockImportFormatService.updateOwn(authentication.getName(), dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN')")
    public ResponseEntity<CompagnieDTO> update(@PathVariable Long id, @RequestBody UpdateCompagnieDTO dto, Authentication authentication) {
        return ResponseEntity.ok(compagnieService.update(id, dto, authentication.getName()));
    }

    @PostMapping("/{id}/reset-admin-password")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN')")
    public ResponseEntity<ResetPasswordResultDTO> resetAdminPassword(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(compagnieService.resetAdminPassword(id, authentication.getName()));
    }

    /** (Re)accorde a l'admin de la compagnie tous les modules/menus applicables a son type de commerce. */
    @PostMapping("/{id}/resynchroniser-modules")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN')")
    public ResponseEntity<Void> resynchroniserModulesAdmin(@PathVariable Long id, Authentication authentication) {
        compagnieService.resynchroniserModulesAdmin(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activer")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN')")
    public ResponseEntity<CompagnieDTO> activer(@PathVariable Long id, @RequestBody ActivationDTO dto, Authentication authentication) {
        return ResponseEntity.ok(compagnieService.setActive(id, true, dto.getMotif(), authentication.getName()));
    }

    @PostMapping("/{id}/desactiver")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN')")
    public ResponseEntity<CompagnieDTO> desactiver(@PathVariable Long id, @RequestBody ActivationDTO dto, Authentication authentication) {
        return ResponseEntity.ok(compagnieService.setActive(id, false, dto.getMotif(), authentication.getName()));
    }
}
