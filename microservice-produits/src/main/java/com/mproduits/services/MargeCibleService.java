package com.mproduits.services;

import com.mproduits.exceptions.BadRequestException;
import com.mproduits.model.Categories;
import com.mproduits.model.MargeCible;
import com.mproduits.repositories.CategorieRepositories;
import com.mproduits.repositories.MargeCibleRepositories;
import com.mproduits.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MargeCibleService {

    private final MargeCibleRepositories repository;
    private final CategorieRepositories categorieRepositories;
    private final TenantContext tenantContext;

    private Long compagnieCourante() {
        Long compagnieId = tenantContext.currentCompagnieId();
        if (compagnieId == null) {
            throw new BadRequestException("Aucune compagnie associee a ce compte");
        }
        return compagnieId;
    }

    @Transactional(readOnly = true)
    public List<MargeCible> findAll() {
        return repository.findByCompagnie_Id(compagnieCourante());
    }

    public MargeCible create(Long categorieId, BigDecimal tauxCible) {
        Long compagnieId = compagnieCourante();
        Categories categorie = categorieRepositories.findByIdAndCompagnie_Id(categorieId, compagnieId)
                .orElseThrow(() -> new EntityNotFoundException("Categorie non trouvee avec l'ID: " + categorieId));

        MargeCible margeCible = new MargeCible();
        margeCible.setCategorie(categorie);
        margeCible.setTauxCible(tauxCible);
        margeCible.setCompagnieId(compagnieId);
        return repository.save(margeCible);
    }

    public MargeCible update(Long id, BigDecimal tauxCible) {
        MargeCible existant = repository.findByIdAndCompagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new EntityNotFoundException("Marge cible non trouvee avec l'ID: " + id));
        existant.setTauxCible(tauxCible);
        return repository.save(existant);
    }

    public void deleteById(Long id) {
        MargeCible existant = repository.findByIdAndCompagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new EntityNotFoundException("Marge cible non trouvee avec l'ID: " + id));
        repository.deleteById(existant.getId());
    }
}
