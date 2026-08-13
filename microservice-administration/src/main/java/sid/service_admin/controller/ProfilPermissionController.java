package sid.service_admin.controller;

import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sid.service_admin.dto.MenuActionsDTO;
import sid.service_admin.dto.TogglePermissionRequest;
import sid.service_admin.model.Profil;
import sid.service_admin.service.ProfilPermissionMatrixService;

/**
 * Administration de la matrice Profil x Menu x Action (voir
 * ProfilPermissionMatrixService). Reserve aux administrateurs : un
 * COMPANY_ADMIN configure les droits des profils de sa propre compagnie de
 * la meme facon qu'un SUPER_ADMIN/SYSTEM_ADMIN.
 */
@RestController
@RequestMapping("/profils")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN','COMPANY_ADMIN')")
public class ProfilPermissionController {

    private final ProfilPermissionMatrixService matrixService;

    @Data
    public static class DupliquerProfilRequest {
        private String code;
        private String description;
    }

    @GetMapping("/{profilId}/permissions-matrix")
    public ResponseEntity<List<MenuActionsDTO>> getMatrice(@PathVariable Long profilId) {
        return ResponseEntity.ok(matrixService.getMatrice(profilId));
    }

    @PutMapping("/permissions-matrix/toggle")
    public ResponseEntity<Void> toggle(@RequestBody TogglePermissionRequest request) {
        matrixService.toggle(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{profilId}/dupliquer")
    public ResponseEntity<Profil> dupliquer(@PathVariable Long profilId, @RequestBody DupliquerProfilRequest request) {
        Profil copie = matrixService.dupliquer(profilId, request.getCode(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(copie);
    }
}
