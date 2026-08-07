package com.mproduits.services;


import com.mproduits.exceptions.BadRequestException;
import com.mproduits.model.Categories;
import com.mproduits.repositories.CategorieRepositories;
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
public class CategorieService {

    private final CategorieRepositories categorieRepository;
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
    public Page<Categories> findAll(Pageable pageable, String search) {
        Long compagnieId = compagnieCourante();
        if (search != null && !search.isEmpty()) {
            return categorieRepository.findBySearchAndCompagnieId(search, compagnieId, pageable);
        }
        return categorieRepository.findByCompagnie_Id(compagnieId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Categories> findAll() {
        return categorieRepository.findByCompagnie_Id(compagnieCourante());
    }

    @Transactional(readOnly = true)
    public Optional<Categories> findById(Long id) {
        return categorieRepository.findByIdAndCompagnie_Id(id, compagnieCourante());
    }

    public Categories save(Categories categorie) {
        categorie.setCompagnieId(compagnieCourante());
        return categorieRepository.save(categorie);
    }

    public Categories update(Long id, Categories categorie) {
        Categories existante = categorieRepository.findByIdAndCompagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new RuntimeException("Categorie not found with id: " + id));
        categorie.setId(id);
        categorie.setCompagnie(existante.getCompagnie());
        return categorieRepository.save(categorie);
    }

    public void deleteById(Long id) {
        Categories existante = categorieRepository.findByIdAndCompagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new RuntimeException("Categorie not found with id: " + id));
        categorieRepository.deleteById(existante.getId());
    }
}
