package com.mproduits.services;


import com.mproduits.exceptions.BadRequestException;
import com.mproduits.exceptions.ConflictException;
import com.mproduits.model.Boutique;
import com.mproduits.repositories.BoutiqueRepositories;
import com.mproduits.security.LicenceCheckResult;
import com.mproduits.security.LicenceStatusService;
import com.mproduits.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BoutiqueService {

    private final BoutiqueRepositories boutiqueRepository;
    private final LicenceStatusService licenceStatusService;
    private final TenantContext tenantContext;

    /** La compagnie n'est jamais fournie par le client - toujours derivee du token JWT de l'appelant. */
    private Long compagnieCourante() {
        Long compagnieId = tenantContext.currentCompagnieId();
        if (compagnieId == null) {
            throw new BadRequestException("Aucune compagnie associee a ce compte");
        }
        return compagnieId;
    }

    /**
     * compagnieId vient de l'attribut de requete pose par JwtAuthFilter (claim
     * du token de l'utilisateur qui cree la boutique) - jamais du corps de la
     * requete, pour ne pas laisser un client choisir la compagnie ciblee.
     */
    public Boutique createForCompagnie(Boutique boutique, Long compagnieId) {
        if (compagnieId != null) {
            LicenceCheckResult result = licenceStatusService.check(compagnieId);
            Integer maxBoutiques = result.getStatut() != null ? result.getStatut().getMaxBoutiques() : null;
            long boutiquesExistantes = boutiqueRepository.countByCompagnieId(compagnieId);
            if (maxBoutiques != null && boutiquesExistantes >= maxBoutiques) {
                throw new ConflictException("Quota de boutiques atteint pour cette compagnie (max " + maxBoutiques + ")");
            }
            boutique.setCompagnieId(compagnieId);
            // La toute premiere boutique d'une compagnie devient automatiquement
            // sa boutique principale (page de vente publique) - a defaut, aucune
            // ne serait selectionnee par defaut.
            boutique.setPrincipale(boutiquesExistantes == 0);
        }
        return boutiqueRepository.save(boutique);
    }

    /**
     * Designe la boutique principale (proposee par defaut sur la page de
     * vente publique) parmi les boutiques de la compagnie courante -
     * demarque automatiquement l'ancienne, une seule principale a la fois.
     */
    public Boutique definirPrincipale(Long boutiqueId) {
        Long compagnieId = compagnieCourante();
        Boutique nouvellePrincipale = boutiqueRepository.findByIdAndCompagnie_Id(boutiqueId, compagnieId)
                .orElseThrow(() -> new RuntimeException("Boutique not found with id: " + boutiqueId));

        boutiqueRepository.findByCompagnie_IdAndPrincipaleTrueAndIdNot(compagnieId, boutiqueId)
                .forEach(b -> {
                    b.setPrincipale(Boolean.FALSE);
                    boutiqueRepository.save(b);
                });

        nouvellePrincipale.setPrincipale(Boolean.TRUE);
        return boutiqueRepository.save(nouvellePrincipale);
    }

    @Transactional(readOnly = true)
    public Page<Boutique> findAll(Pageable pageable, String search) {
        Long compagnieId = compagnieCourante();
        if (search != null && !search.isEmpty()) {
            return boutiqueRepository.findBySearchAndCompagnieId(search, compagnieId, pageable);
        }
        return boutiqueRepository.findByCompagnie_Id(compagnieId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Boutique> findAll() {
        return boutiqueRepository.findByCompagnie_Id(compagnieCourante());
    }

    @Transactional(readOnly = true)
    public Optional<Boutique> findById(Long id) {
        return boutiqueRepository.findByIdAndCompagnie_Id(id, compagnieCourante());
    }

    public Boutique update(Long id, Boutique boutique) {
        Boutique existante = boutiqueRepository.findByIdAndCompagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new RuntimeException("Boutique not found with id: " + id));
        boutique.setId(id);
        // La compagnie n'est jamais modifiable via cet endpoint - on garde celle de la boutique existante,
        // quoi que le client ait envoye dans le corps de la requete.
        boutique.setCompagnie(existante.getCompagnie());
        return boutiqueRepository.save(boutique);
    }

    public void deleteById(Long id) {
        Boutique existante = boutiqueRepository.findByIdAndCompagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new RuntimeException("Boutique not found with id: " + id));
        boutiqueRepository.deleteById(existante.getId());
    }
}
