package com.mproduits.services;

import com.mproduits.dto.VenteParCompagnieDTO;
import com.mproduits.repositories.VenteRepositories;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Vue plateforme reservee aux administrateurs systeme (SUPER_ADMIN/
 * SYSTEM_ADMIN) : chiffres agreges par compagnie uniquement - jamais le
 * detail d'une vente/facture/client precis (voir TenantScopeFilter, qui
 * bloque par ailleurs ces comptes sur tous les endpoints metier detailles).
 */
@Service
@RequiredArgsConstructor
public class PlatformDashboardService {

    private final VenteRepositories venteRepositories;

    @Transactional(readOnly = true)
    public List<VenteParCompagnieDTO> getVentesParCompagnie(Date debut, Date fin) {
        return venteRepositories.aggregerVentesParCompagnie(debut, fin).stream()
                .map(row -> new VenteParCompagnieDTO(
                        (Long) row[0],
                        (String) row[1],
                        (BigDecimal) row[2],
                        (Long) row[3]))
                .collect(Collectors.toList());
    }
}
