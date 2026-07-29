package sid.service_admin.controller;

import java.util.List;
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
import sid.service_admin.dto.CompagnieDTO;
import sid.service_admin.dto.CreateCompagnieDTO;
import sid.service_admin.dto.CreateCompagnieResultDTO;
import sid.service_admin.dto.UpdateCompagnieDTO;
import sid.service_admin.service.CompagnieService;

/**
 * Creation/gestion des compagnies - reserve aux administrateurs
 * systeme/super-administrateur, sauf /me qui est reserve a l'admin compagnie.
 */
@RestController
@RequestMapping("/compagnies")
public class CompagnieController {

    private final CompagnieService compagnieService;

    public CompagnieController(CompagnieService compagnieService) {
        this.compagnieService = compagnieService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN')")
    public ResponseEntity<CreateCompagnieResultDTO> create(@RequestBody CreateCompagnieDTO dto, Authentication authentication) {
        return ResponseEntity.ok(compagnieService.createCompagnieWithAdmin(dto, authentication.getName()));
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

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN')")
    public ResponseEntity<CompagnieDTO> update(@PathVariable Long id, @RequestBody UpdateCompagnieDTO dto) {
        return ResponseEntity.ok(compagnieService.update(id, dto));
    }
}
