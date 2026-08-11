package com.mproduits.services;

import com.mproduits.exceptions.BadRequestException;
import com.mproduits.model.Entreprise;
import com.mproduits.model.Verrouillage;
import com.mproduits.repositories.VerouillaRepositories;
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
public class VerrouillageService {

    private final VerouillaRepositories verouillaRepository;
    private final EntrepriseService entrepriseService;
    private final TenantContext tenantContext;

    /** La compagnie n'est jamais fournie par le client - toujours derivee du token JWT de l'appelant. */
    private Long compagnieCourante() {
        Long compagnieId = tenantContext.currentCompagnieId();
        if (compagnieId == null) {
            throw new BadRequestException("Aucune compagnie associee a ce compte");
        }
        return compagnieId;
    }

    @Transactional(readOnly = true)
    public Page<Verrouillage> findAll(Pageable pageable) {
        return verouillaRepository.findByCompagnieId(compagnieCourante(), pageable);
    }

    @Transactional(readOnly = true)
    public List<Verrouillage> findAll() {
        return verouillaRepository.findByCompagnieId(compagnieCourante());
    }

    @Transactional(readOnly = true)
    public Optional<Verrouillage> findById(Long id) {
        return verouillaRepository.findByIdAndCompagnieId(id, compagnieCourante());
    }

    public Verrouillage save(Verrouillage verrouillage) {
        Entreprise exerciceActif = entrepriseService.obtenirOuCreerExerciceActif(compagnieCourante());
        verrouillage.setEntreprise(exerciceActif);
        verrouillage.setUtilisateur(tenantContext.currentUsername());
        verrouillage.setDateModification(new java.util.Date());
        return verouillaRepository.save(verrouillage);
    }

    public Verrouillage update(Long id, Verrouillage verrouillage) {
        Verrouillage existant = verouillaRepository.findByIdAndCompagnieId(id, compagnieCourante())
                .orElseThrow(() -> new RuntimeException("Verrouillage not found with id: " + id));
        verrouillage.setId(id);
        verrouillage.setEntreprise(existant.getEntreprise());
        verrouillage.setUtilisateur(tenantContext.currentUsername());
        verrouillage.setDateModification(new java.util.Date());
        return verouillaRepository.save(verrouillage);
    }

    public void deleteById(Long id) {
        Verrouillage existant = verouillaRepository.findByIdAndCompagnieId(id, compagnieCourante())
                .orElseThrow(() -> new RuntimeException("Verrouillage not found with id: " + id));
        verouillaRepository.deleteById(existant.getId());
    }
}
