/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.services;


import com.mproduits.enums.TypeMagasin;
import com.mproduits.exceptions.BadRequestException;
import com.mproduits.model.Magasin;
import com.mproduits.repositories.MagasinRepository;
import com.mproduits.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MagasinService {

    private final MagasinRepository magasinRepository;
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
    public Page<Magasin> findAll(Pageable pageable, String search) {
        Long compagnieId = compagnieCourante();
        if (search != null && !search.isEmpty()) {
            return magasinRepository.findBySearchAndCompagnieId(search, compagnieId, pageable);
        }
        return magasinRepository.findByCompagnie_Id(compagnieId, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Magasin> findById(Long id) {
        return magasinRepository.findByIdAndCompagnie_Id(id, compagnieCourante());
    }

    public Magasin save(Magasin magasin) {
        magasin.setCompagnieId(compagnieCourante());
        validerCoherenceType(magasin);
        return magasinRepository.save(magasin);
    }

    public Magasin update(Long id, Magasin magasin) {
        Magasin existant = magasinRepository.findByIdAndCompagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new RuntimeException("Magasin not found with id: " + id));
        magasin.setId(id);
        // La compagnie n'est jamais modifiable via cet endpoint.
        magasin.setCompagnie(existant.getCompagnie());
        validerCoherenceType(magasin);
        return magasinRepository.save(magasin);
    }

    /**
     * Un magasin POINT_DE_VENTE doit avoir une boutique, un DEPOT_CENTRAL ne
     * doit pas en avoir - evite l'etat incoherent que permettait l'ancienne
     * deduction implicite (type jamais verifie, simplement suppose).
     */
    private void validerCoherenceType(Magasin magasin) {
        if (magasin.getTypeMagasin() == TypeMagasin.POINT_DE_VENTE && magasin.getBoutiqueId() == null) {
            throw new BadRequestException("Un magasin de type Point de Vente doit etre rattache a une boutique");
        }
        if (magasin.getTypeMagasin() == TypeMagasin.DEPOT_CENTRAL && magasin.getBoutiqueId() != null) {
            throw new BadRequestException("Un magasin de type Depot Central ne doit pas etre rattache a une boutique");
        }
    }

    public void deleteById(Long id) {
        Magasin existant = magasinRepository.findByIdAndCompagnie_Id(id, compagnieCourante())
                .orElseThrow(() -> new RuntimeException("Magasin not found with id: " + id));
        magasinRepository.deleteById(existant.getId());
    }
}
