package sid.service_admin.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sid.service_admin.dto.CompagnieOverviewDTO;
import sid.service_admin.service.PlatformDashboardService;

/**
 * Dashboard reserve aux administrateurs systeme : vue plateforme (toutes
 * les compagnies), jamais les donnees de gestion d'une compagnie precise.
 */
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN')")
public class PlatformDashboardController {

    private final PlatformDashboardService platformDashboardService;

    @GetMapping("/compagnies-overview")
    public ResponseEntity<List<CompagnieOverviewDTO>> getCompagniesOverview() {
        return ResponseEntity.ok(platformDashboardService.getCompagniesOverview());
    }
}
