package com.mproduits.services;

import com.mproduits.exceptions.BadRequestException;
import com.mproduits.model.TypeDepense;
import com.mproduits.repositories.TypeDepenseRepositories;
import com.mproduits.security.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TypeDepenseService {

    private final TypeDepenseRepositories repository;
    private final TenantContext tenantContext;

    private Long compagnieCourante() {
        Long compagnieId = tenantContext.currentCompagnieId();
        if (compagnieId == null) {
            throw new BadRequestException("Aucune compagnie associee a ce compte");
        }
        return compagnieId;
    }

    @Transactional(readOnly = true)
    public List<TypeDepense> findAll() {
        return repository.findByCompagnie_Id(compagnieCourante());
    }

    public TypeDepense create(TypeDepense typeDepense) {
        typeDepense.setId(null);
        typeDepense.setCompagnieId(compagnieCourante());
        return repository.save(typeDepense);
    }

    public TypeDepense update(Long id, TypeDepense typeDepense) {
        TypeDepense existant = repository.findByIdAndCompagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new EntityNotFoundException("Type depense non trouve avec l'ID: " + id));
        existant.setCode(typeDepense.getCode());
        existant.setLibelle(typeDepense.getLibelle());
        return repository.save(existant);
    }

    public void deleteById(Long id) {
        TypeDepense existant = repository.findByIdAndCompagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new EntityNotFoundException("Type depense non trouve avec l'ID: " + id));
        repository.deleteById(existant.getId());
    }
}
