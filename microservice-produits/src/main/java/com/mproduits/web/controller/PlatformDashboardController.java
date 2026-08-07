package com.mproduits.web.controller;

import com.mproduits.dto.VenteParCompagnieDTO;
import com.mproduits.services.PlatformDashboardService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dashboard reserve aux administrateurs systeme : chiffres agreges par
 * compagnie uniquement (voir PlatformDashboardService). Exempte de
 * TenantScopeFilter (voir cette classe) car ces comptes n'ont pas de
 * compagnieId - la protection ici passe par @PreAuthorize.
 */
@RestController
@RequestMapping("/microservice-produits/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','SYSTEM_ADMIN')")
public class PlatformDashboardController {

    private final PlatformDashboardService platformDashboardService;

    @GetMapping("/ventes-par-compagnie")
    public ResponseEntity<List<VenteParCompagnieDTO>> getVentesParCompagnie(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate debut,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fin) {

        LocalDate finEffective = fin != null ? fin : LocalDate.now();
        LocalDate debutEffectif = debut != null ? debut : finEffective.withDayOfMonth(1);

        Date debutDate = Date.from(debutEffectif.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date finDate = Date.from(finEffective.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        return ResponseEntity.ok(platformDashboardService.getVentesParCompagnie(debutDate, finDate));
    }
}
