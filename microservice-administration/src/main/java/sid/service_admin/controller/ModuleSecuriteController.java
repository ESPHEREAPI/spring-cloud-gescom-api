package sid.service_admin.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sid.service_admin.dto.MenuActionsDTO;
import sid.service_admin.service.ProfilPermissionMatrixService;

/**
 * Catalogue de reference Module -> Menu -> Actions disponibles (ecran
 * "Module Securite" du sidebar) - vue en lecture seule, independante de tout
 * profil. Pour la matrice de droits accordes a un profil precis, voir
 * ProfilPermissionController.
 */
@RestController
@RequestMapping("/modules-securite")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN','COMPANY_ADMIN')")
public class ModuleSecuriteController {

    private final ProfilPermissionMatrixService matrixService;

    @GetMapping("/catalogue")
    public ResponseEntity<List<MenuActionsDTO>> getCatalogue() {
        return ResponseEntity.ok(matrixService.getCatalogue());
    }
}
