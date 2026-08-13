package sid.service_admin.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sid.service_admin.model.Action;

/**
 * Catalogue global des Actions (voir Action.java / ActionService). Lecture
 * ouverte a tout utilisateur authentifie (necessaire pour afficher la
 * matrice de permissions) ; creation/suppression reservees a
 * SUPER_ADMIN/SYSTEM_ADMIN (catalogue plateforme, pas propre a une compagnie).
 */
@RestController
@RequestMapping("/actions")
@RequiredArgsConstructor
public class ActionController {

    private final sid.service_admin.service.ActionService service;

    @lombok.Data
    public static class ActionCreateRequest {
        private String code;
        private String libelle;
        private String description;
    }

    @GetMapping
    public ResponseEntity<List<Action>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN')")
    public ResponseEntity<Action> create(@RequestBody ActionCreateRequest request) {
        Action created = service.create(request.getCode(), request.getLibelle(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
