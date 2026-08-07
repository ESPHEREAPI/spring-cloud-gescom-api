package com.mproduits.services;

import com.mproduits.exceptions.BadRequestException;
import com.mproduits.model.Fournisseur;
import com.mproduits.repositories.FournisseurRepositories;
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
public class FournisseurService {

    private final FournisseurRepositories fournisseurRepository;
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
    public Page<Fournisseur> findAll(Pageable pageable, String search) {
        Long compagnieId = compagnieCourante();
        if (search != null && !search.isEmpty()) {
            return fournisseurRepository.findBySearchAndCompagnieId(search, compagnieId, pageable);
        }
        return fournisseurRepository.findByCompagnie_Id(compagnieId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Fournisseur> findAll() {
        return fournisseurRepository.findByCompagnie_Id(compagnieCourante());
    }

    @Transactional(readOnly = true)
    public Optional<Fournisseur> findById(Long id) {
        return fournisseurRepository.findByIdAndCompagnie_Id(id, compagnieCourante());
    }

    public Fournisseur save(Fournisseur fournisseur) {
        fournisseur.setCompagnieId(compagnieCourante());
        return fournisseurRepository.save(fournisseur);
    }

    public Fournisseur update(Long id, Fournisseur fournisseur) {
        Fournisseur existant = fournisseurRepository.findByIdAndCompagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new RuntimeException("Fournisseur not found with id: " + id));
        fournisseur.setId(id);
        fournisseur.setCompagnie(existant.getCompagnie());
        return fournisseurRepository.save(fournisseur);
    }

    public void deleteById(Long id) {
        Fournisseur existant = fournisseurRepository.findByIdAndCompagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new RuntimeException("Fournisseur not found with id: " + id));
        fournisseurRepository.deleteById(existant.getId());
    }
}
