package com.mproduits.services;

import com.mproduits.exceptions.BadRequestException;
import com.mproduits.model.TypeResource;
import com.mproduits.repositories.TypeResourceRepositories;
import com.mproduits.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TypeResourceService {

    private final TypeResourceRepositories repository;
    private final TenantContext tenantContext;

    private Long compagnieCourante() {
        Long compagnieId = tenantContext.currentCompagnieId();
        if (compagnieId == null) {
            throw new BadRequestException("Aucune compagnie associee a ce compte");
        }
        return compagnieId;
    }

    @Transactional(readOnly = true)
    public List<TypeResource> findAll() {
        return repository.findByCompagnie_Id(compagnieCourante());
    }

    public TypeResource create(TypeResource typeResource) {
        typeResource.setId(null);
        typeResource.setCompagnieId(compagnieCourante());
        return repository.save(typeResource);
    }

    public TypeResource update(Long id, TypeResource typeResource) {
        TypeResource existant = repository.findByIdAndCompagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new EntityNotFoundException("Type ressource non trouve avec l'ID: " + id));
        existant.setCode(typeResource.getCode());
        existant.setLibelle(typeResource.getLibelle());
        return repository.save(existant);
    }

    public void deleteById(Long id) {
        TypeResource existant = repository.findByIdAndCompagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new EntityNotFoundException("Type ressource non trouve avec l'ID: " + id));
        repository.deleteById(existant.getId());
    }
}
