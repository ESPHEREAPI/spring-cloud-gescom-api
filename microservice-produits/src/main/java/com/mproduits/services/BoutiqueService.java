package com.mproduits.services;


import com.mproduits.exceptions.ConflictException;
import com.mproduits.model.Boutique;
import com.mproduits.repositories.BoutiqueRepositories;
import com.mproduits.security.LicenceCheckResult;
import com.mproduits.security.LicenceStatusService;
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

    /**
     * compagnieId vient de l'attribut de requete pose par JwtAuthFilter (claim
     * du token de l'utilisateur qui cree la boutique) - jamais du corps de la
     * requete, pour ne pas laisser un client choisir la compagnie ciblee.
     */
    public Boutique createForCompagnie(Boutique boutique, Long compagnieId) {
        if (compagnieId != null) {
            LicenceCheckResult result = licenceStatusService.check(compagnieId);
            Integer maxBoutiques = result.getStatut() != null ? result.getStatut().getMaxBoutiques() : null;
            if (maxBoutiques != null && boutiqueRepository.countByCompagnieId(compagnieId) >= maxBoutiques) {
                throw new ConflictException("Quota de boutiques atteint pour cette compagnie (max " + maxBoutiques + ")");
            }
            boutique.setCompagnieId(compagnieId);
        }
        return boutiqueRepository.save(boutique);
    }

    public Page<Boutique> findAll(Pageable pageable, String search) {
        if (search != null && !search.isEmpty()) {
            return boutiqueRepository.findBySearch(search, pageable);
        }
        return boutiqueRepository.findAll(pageable);
    }
    
    public List<Boutique> findAll() {
        return boutiqueRepository.findAll();
    }
    
    public Optional<Boutique> findById(Long id) {
        return boutiqueRepository.findById(id);
    }
    
    public Boutique update(Long id, Boutique boutique) {
        if (!boutiqueRepository.existsById(id)) {
            throw new RuntimeException("Boutique not found with id: " + id);
        }
        boutique.setId(id);
        return boutiqueRepository.save(boutique);
    }
    
    public void deleteById(Long id) {
        if (!boutiqueRepository.existsById(id)) {
            throw new RuntimeException("Boutique not found with id: " + id);
        }
        boutiqueRepository.deleteById(id);
    }
}