package com.mproduits.services;


import com.mproduits.exceptions.BadRequestException;
import com.mproduits.model.Specifique;
import com.mproduits.repositories.SpecifiqueRepositories;
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
public class SpecifiqueService {

    private final SpecifiqueRepositories specifiqueRepository;
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
    public Page<Specifique> findAll(Pageable pageable, String search) {
        Long compagnieId = compagnieCourante();
        if (search != null && !search.isEmpty()) {
            return specifiqueRepository.findBySearchAndCompagnieId(search, compagnieId, pageable);
        }
        return specifiqueRepository.findByCompagnie_Id(compagnieId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Specifique> findAll() {
        return specifiqueRepository.findByCompagnie_Id(compagnieCourante());
    }

    @Transactional(readOnly = true)
    public Optional<Specifique> findById(Long id) {
        return specifiqueRepository.findByIdAndCompagnie_Id(id, compagnieCourante());
    }

    public Specifique save(Specifique specifique) {
        specifique.setCompagnieId(compagnieCourante());
        return specifiqueRepository.save(specifique);
    }

    public Specifique update(Long id, Specifique specifique) {
        Specifique existant = specifiqueRepository.findByIdAndCompagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new RuntimeException("Specifique not found with id: " + id));
        specifique.setId(id);
        specifique.setCompagnie(existant.getCompagnie());
        return specifiqueRepository.save(specifique);
    }

    public void deleteById(Long id) {
        Specifique existant = specifiqueRepository.findByIdAndCompagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new RuntimeException("Specifique not found with id: " + id));
        specifiqueRepository.deleteById(existant.getId());
    }
}
